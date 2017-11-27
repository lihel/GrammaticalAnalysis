/**
 * Created by lmy on 17-11-27.
 */
public class TableE {
    private String nT;
    private char yT;
    private String exp;
    public TableE(String nt,char c,String exp){
        this.yT=c;
        this.nT=nt;
        this.exp=exp;
    }
    public boolean isEquals(TableE t){//判断两个表格元素是否完全相同
        if(t.nT.equals(this.nT)&&t.yT==this.yT&&t.exp.equals(this.exp))
            return true;
        return false;
    }

    public void print(){//打印当前的表格对象
        System.out.println("["+this.nT+"] ["+this.yT+"] ("+this.exp+ ") ");
    }
    public String getNT() {//获得非终结符。
        return this.nT;
    }
}