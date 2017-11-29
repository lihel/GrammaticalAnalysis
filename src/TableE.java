/**
 * Created by lmy on 17-11-27.
 */
public class TableE {
    private String nT;
    private char yT;
    private String exp;

    public TableE(String nt, char c, String exp) {
        this.yT = c;
        this.nT = nt;
        this.exp = exp;
    }

    public boolean isEquals(TableE t) {//判断两个表格元素是否完全相同
        if (t.nT.equals(this.nT) && t.yT == this.yT && t.exp.equals(this.exp))
            return true;
        return false;
    }

    public void print() {//打印当前的表格对象

        System.out.printf("%-10s %-20s %-20s\n", this.nT, this.yT, this.exp);

        // System.out.println("["+this.nT+"] ["+this.yT+"] ("+this.exp+ ") ");
    }

    public String getNT() {//获得非终结符。
        return this.nT;
    }

    public char getyT() {//获得终结符
        return this.yT;
    }
    public String getT(char yT){
        if(this.yT==yT){
            return this.exp;
        }
        return "";
    }
}