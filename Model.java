import java.util.*;

public class Model {
    private Tile[][] gameTiles;
    private static final int FIELD_WIDTH = 4;

    private Stack previousStates = new Stack();
    private Stack previousScores = new Stack();

    private boolean isSaveNeeded = true;

    public int score;
    public int maxTile;

    private void saveState (Tile [][] gameTiles) {
        Tile [][] tiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++){
                tiles[i][j] = new Tile(gameTiles[i][j].value);
            }
        }
        previousStates.push(tiles);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback () {
        if (previousScores.isEmpty() || previousStates.isEmpty()) {
            return;
        }
        gameTiles = (Tile[][]) previousStates.pop();
        score = (int) previousScores.pop();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public boolean canMove () {
        List <Tile> emptyTiles = getEmptyTiles();
        if (emptyTiles.size() > 0) {
            return true;
        }
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++){
                if (i == 0 && j == 0 || i == 0 && j == 3 || i == 3 && j == 0 || i == 3 && j == 3){
                    continue;
                }
                if (i == 0) {
                    if (gameTiles[i][j].value == gameTiles[i + 1][j].value ||
                            gameTiles[i][j].value == gameTiles[i][j - 1].value ||
                            gameTiles[i][j].value == gameTiles[i][j + 1].value) {
                        return true;
                    }
                    continue;
                }
                if (i == 3) {
                    if (gameTiles[i][j].value == gameTiles[i - 1][j].value ||
                            gameTiles[i][j].value == gameTiles[i][j - 1].value ||
                            gameTiles[i][j].value == gameTiles[i][j + 1].value) {
                        return true;
                    }
                    continue;
                }
                if (j == 0) {
                    if (gameTiles[i][j].value == gameTiles[i + 1][j].value ||
                            gameTiles[i][j].value == gameTiles[i - 1][j].value ||
                            gameTiles[i][j].value == gameTiles[i][j + 1].value) {
                        return true;
                    }
                    continue;
                }
                if (j == 3) {
                    if (gameTiles[i][j].value == gameTiles[i - 1][j].value ||
                            gameTiles[i][j].value == gameTiles[i + 1][j].value ||
                            gameTiles[i][j].value == gameTiles[i][j - 1].value) {
                        return true;
                    }
                    continue;
                }
                if (gameTiles[i][j].value == gameTiles[i - 1][j].value ||
                gameTiles[i][j].value == gameTiles[i + 1][j].value ||
                gameTiles[i][j].value == gameTiles[i][j - 1].value ||
                gameTiles[i][j].value == gameTiles[i][j + 1].value) {
                    return true;
                }
            }
        }
        return false;
    }


    public Model() {
        resetGameTiles();
    }

    void resetGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
        score = 0;
        maxTile = 0;
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (!emptyTiles.isEmpty()) {
            int index = (int) (Math.random() * emptyTiles.size()) % emptyTiles.size();
            Tile emptyTile = emptyTiles.get(index);
            emptyTile.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private List<Tile> getEmptyTiles() {
        final List<Tile> list = new ArrayList<Tile>();
        for (Tile[] tileArray : gameTiles) {
            for (Tile t : tileArray)
                if (t.isEmpty()) {
                    list.add(t);
                }
        }
        return list;
    }

    private boolean compressTiles (Tile [] tiles) {
        int indexNewTile = 0;
        boolean isCompressing = false;
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i].value != 0) {
                if (i != indexNewTile) {
                    tiles[indexNewTile] = tiles[i];
                    tiles[i] = new Tile(0);
                    isCompressing = true;
                }
                indexNewTile++;
            }
        }
        return isCompressing;
    }

    private boolean mergeTiles (Tile [] tiles) {
        boolean isMerging = false;
        for (int i = 1; i < tiles.length; i++) {
            if ((tiles[i].value == tiles[i - 1].value) && (tiles[i].value != 0)) {
                int newScoreTile = tiles[i].value * 2;
                tiles[i - 1].value = newScoreTile;
                tiles[i].value = 0;
                isMerging = true;
                score += newScoreTile;
                if (maxTile < newScoreTile) {
                    maxTile = newScoreTile;
                }
            }
        }
        compressTiles(tiles);
        return isMerging;
    }

    public void left () {
        if (isSaveNeeded) {
            saveState(gameTiles);
        }
        boolean flag = false;
        for (int i = 0; i < gameTiles.length; i++) {
            boolean isCompressing = compressTiles(gameTiles[i]);
            boolean isMerging = mergeTiles(gameTiles[i]);
            isSaveNeeded = true;
            if (isCompressing | isMerging) {
                flag = true;
            }
        }
        if (flag) {
            addTile();
        }
    }

    public void right () {
        saveState(gameTiles);
        turn();
        turn();
        left();
        turn();
        turn();
    }

    public void down () {
        saveState(gameTiles);
        turn();
        left();
        turn();
        turn();
        turn();
    }

    public void up () {
        saveState(gameTiles);
        turn();
        turn();
        turn();
        left();
        turn();
    }

    public void turn () {
        Tile [] [] tilesOriginal = new Tile[4][4];

        for (int i = 0; i < FIELD_WIDTH; i++){
            for (int j = 0; j < FIELD_WIDTH; j++) {
                tilesOriginal[i][j] = gameTiles[i][j];
            }
        }

        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = tilesOriginal[FIELD_WIDTH - 1 - j][i];
            }
        }

    }

    public void randomMove () {
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n) {
            case 0:
                left();
                break;

            case 1:
                right();
                break;

            case 2:
                up();
                break;

            case 3:
                down();
                break;
        }
    }

    private int weightTiles (Tile [][] tiles) {
        int result = 0;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                result += tiles[i][j].value;
            }
        }
        return result;
    }

    public boolean hasBoardChanged () {
        int gameWeight = weightTiles(gameTiles);
        Tile[][] previousStateTiles = (Tile[][]) previousStates.peek();
        int previousGameWeight = weightTiles(previousStateTiles);

        return gameWeight != previousGameWeight;
    }

    public MoveEfficiency getMoveEfficiency (Move move) {
        MoveEfficiency moveEfficiency = new MoveEfficiency(-1, 0, move);
        move.move();
        if (hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        }
        rollback();
        return moveEfficiency;
    }

    public void autoMove() {
        PriorityQueue <MoveEfficiency> queue = new PriorityQueue<MoveEfficiency>(4, Collections.reverseOrder());
        queue.offer(getMoveEfficiency(this::left));
        queue.offer(getMoveEfficiency(this::right));
        queue.offer(getMoveEfficiency(this::down));
        queue.offer(getMoveEfficiency(this::up));
        Move move = queue.peek().getMove();
        move.move();

    }
}
