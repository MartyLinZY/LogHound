public class Test {
    private int x;
    private int y;

    public Test(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int add() {
        return this.x + this.y;
    }

    public int getX() {
        add();
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }
}