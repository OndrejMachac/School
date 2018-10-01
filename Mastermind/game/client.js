game = {
    settings: null,
    step: 1
}

var doneBtn = document.createElement("div");
doneBtn.className = "big circle";
doneBtn.id = "done";
doneBtn.innerHTML = '<div></div>';

function gameAjax(action, data, callback) {
    if (action == null) { return false; }

    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200 && callback != null) {
            let data = null
            try {
                if(this.responseText != "") {
                    data = JSON.parse(this.responseText);
                }
            } catch (err) {
                console.log(this.responseText);
                console.log(err);
            }
            callback(data)
        }
    };
    xmlhttp.open("GET", "game/server.php?" + "action=" + action + (data != null ? "&data=" + JSON.stringify(data) : ""));
    xmlhttp.send();
}

//----- TABLE MODIFIERS -----
function createTable(rows, columns) {
    clearTable();
    let body = e("mastermind").tBodies[0];

    //Answer header
    let row = e("mastermind").tHead.rows[0];
    row.cells[0].colSpan = columns + 1
    for (let i = 0; i < columns; i++) {
        row.insertCell(row.cells.length - 1).innerHTML = '<div class="big pin">?</div>';
    }
    //Body
    for (let row = 0; row < rows; row++) {
        tableRow = body.insertRow(0);

        //Attempt number
        tableRow.insertCell(-1).innerHTML = '<div class="attempt circle">' + (row + 1) + '</div>';

        //Small pin holes
        for (let column = 0; column < columns; column++) {
            tableRow.insertCell(-1).innerHTML = '<div class="small pin"></div>'
        }
        //Big pin holes
        for (let column = 0; column < columns; column++) {
            tableRow.insertCell(-1).innerHTML = '<div class="big pin hole"></div>'
        }

        //Confirm button
        if (row == 0) {
            tableRow.insertCell(-1).appendChild(doneBtn);
        } else {
            tableRow.insertCell(-1);
        }
    }
}

function clearTable() {
    //Remove whole body
    e("mastermind").tBodies[0].innerHTML = "";

    //Remove answer cells
    let head = e("mastermind").tHead.rows[0];

    while (head.cells.length > 2) {
        head.deleteCell(1);
    }
}

function addSmallPins(data, row) {
    let pins = row.getElementsByClassName("small pin");

    //Set black and white pins
    for (var i = 0; i < pins.length; i++) {
        if (data.black-- > 0) {
            pins[i].dataset.color = 8;
        } else if (data.white-- > 0) {
            pins[i].dataset.color = 7;
        }
    }
}

function showResult(result) {
    let result_pins = e("mastermind").tHead.rows[0].getElementsByClassName("big pin");

    for (var i = 0; i < result.length; i++) {
        result_pins[i].dataset.color = result[i];
        result_pins[i].innerHTML = "";
    }
    //Remove done button
    var element = document.getElementById('done');
    element.parentNode.removeChild(element);
    setAction("fi-refresh");
}

function addStep(data) { 
    var rows = e("mastermind").tBodies[0].rows;

    addSmallPins(data, rows[rows.length - game.step]);

    if (data.result == null) {
        //Move done button
        var r = rows[rows.length - ++game.step].cells;
        r[r.length - 1].appendChild(document.getElementById('done'));
    } else {
        showResult(data.result);
        if (data.black == game.settings.rows) {
            //win
        } else {
            //lose
        }
        game.settings = null;
    }
}

function addColors(colors, num) {
    var pins = e("mastermind").tBodies[0].rows[e("mastermind").tBodies[0].rows.length - num - 1].getElementsByClassName("big pin");

    for (var i = 0; i < pins.length; i++) {
        if (colors[i] != 0) {
            pins[i].classList.remove("hole");
            pins[i].dataset.color = colors[i];
        }
    }
}

//----- GAME BUTTONS  ----- 
doneBtn.addEventListener('click', function() {
    var pins = e("mastermind").tBodies[0].rows[game.settings.rows - game.step].getElementsByClassName("big pin");
    var step = [];
    for (var i = 0; i < pins.length; i++) {
        if (pins[i].dataset.color == null) {
            if (!game.settings.empty) {
                return;
            }
            step.push(0);
        } else {
            step.push(parseInt(pins[i].dataset.color));
        }
    }
    for (let i = 0; i < pins.length; i++) {
        pins[i].classList.remove("hole");
    }
    gameAjax("step", step, addStep);
});

//----- PIN DRAG AND DROP -----
var pin = { sample: null, last: null }
document.addEventListener('mousedown', function(event) {
    //Create draggable pin
    if (event.button == 0 && event.target.dataset.color != null && (event.target.classList.contains("hole") || event.target.classList.contains("sample"))) {
        pin.sample = event.target.cloneNode(true);
        pin.sample.classList.add("draggable");
        document.body.appendChild(pin.sample);

        //If target wasn't sample, remove target
        if (event.target.classList.contains("hole")) {
            event.target.removeAttribute("data-color");
            pin.last = event.target;
        }
        updatePosition(event);
    }
});

document.addEventListener('mouseup', function(event) {
    if (pin.sample != null) {
        document.body.removeChild(pin.sample);

        //Get target
        var target = document.elementFromPoint(event.clientX, event.clientY);
        if (target.tagName == 'TD') { target = target.childNodes[0]; }

        if (target != null && target.classList.contains("hole")) {
            //Place pin from target hole to last hole
            if (target.dataset.color != null && pin.last != null) {
                pin.last.dataset.color = target.dataset.color;
                pin.last = null;
            }
            //Replace target pin with new pin
            target.dataset.color = pin.sample.dataset.color;
        }

        pin.sample = null;
    }
});

document.addEventListener('mousemove', function(event) {
    if (pin.sample != null) { updatePosition(event); }
});

function updatePosition(event) { // add scroll
    pin.sample.style.left = (event.clientX - pin.sample.offsetWidth / 2) + 'px';
    pin.sample.style.top = (event.clientY - pin.sample.offsetHeight / 2 + window.scrollY) + 'px';
}

e("rows_slider").oninput = function() {
    e("rows").innerHTML = this.value;
}
e("columns_slider").oninput = function() {
    e("columns").innerHTML = this.value;
}

//New game BUTTON
e("start").addEventListener("click", function() {
    newGame();
    e("settings").style.display = "none";
});

e("back").addEventListener("click", function() {
    e("settings").style.display = "none";
});

//Settings BUTTON
e("game-settings").addEventListener("click", function() {
    e("settings").style.display = "";
});

//Action BUTTON
e("game-action").addEventListener("click", function() {
    if (!e("game-action").classList.contains("disabled")) {
        if (game.settings == null) {
            newGame();
            setAction("fi-flag");
        } else {
            gameAjax("surrender", null, showResult);
            setAction("fi-refresh");
            game.settings = null;
        }
    }
});

function setAction(action){
    delayAction();
    e("game-action").innerHTML = '<i class="game-button step ' + action + '"></i>';
}

function delayAction(){
    e("game-action").classList.add("disabled");

    setTimeout(() => {
        e("game-action").classList.remove("disabled");
    }, 1000);
}

function newGame() {
    game.settings = {
        repeat: e("repeat").checked,
        empty: e("empty").checked,
        rows: parseInt(e("rows_slider").value),
        columns: parseInt(e("columns_slider").value),
        colors: 8
    };
    gameAjax("newGame", (game.settings != null ? game.settings : ""), function() {
        createTable(game.settings.rows, game.settings.columns);
    });
    game.step = 1;
}

gameAjax("getGame", null, function(data) {
    if (data != null) {
        game.settings = {
            repeat: data.repeat,
            empty: data.empty,
            rows: data.rows,
            columns: data.columns,
            colors: data.colors
        }
        createTable(game.settings.rows, game.settings.columns);

        if (data.steps != null) {
            for (let step = 0; step < data.steps.length; step++) {
                addSmallPins(data.steps[step], e("mastermind").tBodies[0].rows[e("mastermind").tBodies[0].rows.length - step - 1]);
                addColors(data.steps[step].colors, step);
            }
            game.step = data.steps.length + 1;

            var rows = e("mastermind").tBodies[0].rows;
            var r = rows[rows.length - game.step].cells;
            r[r.length - 1].appendChild(document.getElementById('done'));
        }
    } else {
        newGame();
    }
});

//----- GLOBAL -----
function e(id) {
    return document.getElementById(id);
}
