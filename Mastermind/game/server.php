<?PHP
session_start();
error_reporting(0);

//Default settings
define("REPEAT", false);
define("ALLOW_EMPTY", false);
define("COLUMNS", 5);
define("ROWS", 10);
define("COLORS", 8);

//--- ACTION HANDLER ---
if (!empty($_GET['action'])) {
    $action = $_GET['action'];

    //Call function that matches action
    if (is_callable($action)) {
        if (!empty($_GET['data'])) {
            $action(json_decode($_GET['data']));
        } else {
            $action();
        }
    } else {
        echo 'Invalid action!';
    }
}

//--- GAME ERASE ---
function eraseGame() {
    $_SESSION['result'] = array();
    $_SESSION['steps'] = array();
    $_SESSION['repeat'] = null;
    $_SESSION['rows'] = null;
    $_SESSION['columns'] = null;
    $_SESSION['colors'] = null;
}

//--- GAME CREATOR ---
function newGame($data)
{
    eraseGame();

    //Set game settings
    $_SESSION['repeat'] = isset($data->repeat) ? $data->repeat : REPEAT;
    $_SESSION['empty'] = isset($data->empty) ? $data->empty : ALLOW_EMPTY;
    $_SESSION['rows'] = isset($data->rows) ? $data->rows : ROWS;
    $_SESSION['columns'] = isset($data->columns) ? $data->columns : COLUMNS;
    $_SESSION['colors'] = isset($data->colors) ? $data->colors : COLORS;

    //Generate colors
    for ($c = 0; $c < $_SESSION['columns']; $c++) {
        do {
            $color = rand(1, $_SESSION['colors']);
        } while (!$_SESSION['repeat'] && in_array($color, $_SESSION['result']));
        array_push($_SESSION['result'], $color);
    }
}

//--- STEP HANDLER ---
function step($data)
{
    if (empty($_SESSION['result'])) {
        echo 'Error! You need to first create new game!';
    } else {
        if (count($data) == $_SESSION['columns']) {
            if (!$_SESSION["empty"] && count(array_keys($data, 0))) {
                echo "Empty pins aren't allowed!";
                return;
            }
            $step = new Step($data);
            array_push($_SESSION['steps'], $step);

            //On game lose or win
            if (count($_SESSION['steps']) >= $_SESSION['rows'] || $step->black == $_SESSION['columns']) {
                $step->result = $_SESSION['result'];
                eraseGame();
            }

            echo json_encode(get_object_vars($step));
        } else {
            echo 'Error! Passed ' . count($data) . ' columns instead of ' . $_SESSION['columns'] . '.';
        }
    }
}

//--- SURRENDER ---
function surrender()
{
    echo json_encode($_SESSION['result']);
    eraseGame();
}

//--- GET GAME ---
function getGame()
{
    if (!empty($_SESSION['colors'])) {
        $data = array();
        $data["repeat"] = $_SESSION['repeat'];
        $data["rows"] = $_SESSION['rows'];
        $data["columns"] = $_SESSION['columns'];
        $data["colors"] = $_SESSION['colors'];
        $data["empty"] = $_SESSION['empty'];
        foreach ($_SESSION['steps'] as $key => $step) {
            $data["steps"][$key] = get_object_vars($step);
            $data["steps"][$key]["colors"] = $step->getColors();
        }
        echo json_encode($data);
    }
}

//--- STEP OBJECT ---
class Step
{
    public $black = 0;
    public $white = 0;
    private $colors = array();

    public function __construct($colors)
    {
        $this->colors = $colors;
        //Black counter
        foreach ($colors as $key => $color) {
            if ($color == $_SESSION['result'][$key]) {
                $this->black++;
                unset($colors[$key]);
            }
        }
        //White counter
        foreach ($colors as $color) {
            foreach ($_SESSION['result'] as $result) {
                if ($result == $color) {
                    $this->white++;
                    break;
                }
            }
        }
    }

    public function getColors()
    {
        return $this->colors;
    }
}
