/**
 * Created by lmy on 17-11-25.
 * LL(1)文法　预测分析法
 * E->TE'
 * E'->+TE'|ε
 * T->FT'
 * T'->*FT'|ε
 * F->(E)|i
 */

import java.io.*;
import java.util.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.lang.*;

public class Analysis {

    private static List<String> gra = new ArrayList();
    private static int VTN = 0, VNN = 0;

    private List<String> getGrammar() throws IOException {
        File file = new File("src/grammer.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileReader reader = new FileReader(file);
        BufferedReader bReader = new BufferedReader(reader);//文件缓存
        String s = "";
        while ((s = bReader.readLine()) != null) {
            gra.add(s);
        }
        bReader.close();
        System.out.println(gra);
        return null;
    }

    //终结符
    //private static String[] VT = new String[]{"i", "+", "*", "(", ")", "#"};;
    private static String[] VT = new String[]{"i", "+", "*", "(", ")", "#"};
    ;

    //非终结符
    private String[] VN = new String[]{"E", "E'", "T", "T'", "F"};
    //private static String[] VN = new String[15];

    //输入串strToken
    private StringBuilder strToken = new StringBuilder("i*i+i");

    public static String[] getStr(String[] str) {
        for (int i = 0; i < str.length; i++) {
            System.out.println(str[i]);
            //System.out.println(str[0]);
            //System.out.println("hahhahhahshh---");
        }
        return str;
    }

    public static List<String> getFirst(List<String> gra) {
        String[] right;
        for (int i = 0; i < gra.size(); i++) { // 初始化FIRST集
            right = gra.get(i).split("->");
            System.out.println(getStr(right));
            // System.out.println(getStr(right));


        }
        return gra;
    }

    //预测分析表
    private String[][] analysisTable = new String[][]{
            {"TE'", "", "", "TE'", "", ""},
            {"", "+TE'", "", "", "ε", "ε"},
            {"FT'", "", "", "FT'", "", ""},
            {"", "ε", "*FT'", "", "ε", "ε"},
            {"i", "", "", "(E)", "", ""}
    };


    //分析栈stack
    private Deque<String> stack = new ArrayDeque<>();

    //shuru1保存从输入串中读取的一个输入符号，当前符号
    private String shuru1 = null;

    //X中保存stack栈顶符号
    private String X = null;

    //flag标志预测分析是否成功
    private boolean flag = true;

    //记录输入串中当前字符的位置
    private int cur = 0;

    //记录步数
    private int count = 0;

    public static void main(String[] args) {
        Analysis ll1 = new Analysis();
        try {
            List<String> gra = ll1.getGrammar();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ll1.getFirst(gra);
        ll1.init();
        ll1.totalControlProgram();
        ll1.printf();
    }

    //初始化
    private void init() {
        strToken.append("#");
        stack.push("#");
        System.out.printf("%-8s %-18s %-17s %s\n", "步骤 ", "符号栈 ", "输入串 ", "所用产生式 ");
        stack.push("E");
        curCharacter();
        System.out.printf("%-10d %-20s %-20s\n", count, stack.toString(), strToken.substring(cur, strToken.length()));
    }

    //读取当前栈顶符号
    private void stackPeek() {
        X = stack.peekFirst();
    }

    //返回输入串中当前位置的字母
    private String curCharacter() {
        shuru1 = String.valueOf(strToken.charAt(cur));
        return shuru1;
    }

    //判断X是否是终结符
    private boolean XisVT() {
        for (int i = 0; i < (VT.length - 1); i++) {
            if (VT[i].equals(X)) {
                return true;
            }
        }
        return false;
    }

    //查找X在非终结符中分析表中的横坐标
    private String VNTI() {
        int Ni = 0, Tj = 0;
        for (int i = 0; i < VN.length; i++) {
            if (VN[i].equals(X)) {
                Ni = i;
            }
        }
        for (int j = 0; j < VT.length; j++) {
            if (VT[j].equals(shuru1)) {
                Tj = j;
            }
        }
        return analysisTable[Ni][Tj];
    }

    //判断M[A,a]={X->X1X2...Xk}
    //把X1X2...Xk推进栈
    //X1X2...Xk=ε，不推什么进栈
    private boolean productionType() {
        return VNTI() != "";
    }

    //推进stack栈
    private void pushStack() {
        stack.pop();
        String M = VNTI();
        String ch;
        //处理TE' FT' *FT'特殊情况
        switch (M) {
            case "TE'":
                stack.push("E'");
                stack.push("T");
                break;
            case "FT'":
                stack.push("T'");
                stack.push("F");
                break;
            case "*FT'":
                stack.push("T'");
                stack.push("F");
                stack.push("*");
                break;
            case "+TE'":
                stack.push("E'");
                stack.push("T");
                stack.push("+");
                break;
            default:
                for (int i = (M.length() - 1); i >= 0; i--) {
                    ch = String.valueOf(M.charAt(i));
                    stack.push(ch);
                }
                break;
        }
        System.out.printf("%-10d %-20s %-20s %s->%s\n", (++count), stack.toString(), strToken.substring(cur, strToken.length()), X, M);
    }

    //总控程序
    private void totalControlProgram() {
        while (flag) {
            stackPeek();  //读取当前栈顶符号  令X=栈顶符号
            if (XisVT()) {
                if (X.equals(shuru1)) {
                    cur++;
                    shuru1 = curCharacter();
                    stack.pop();
                    System.out.printf("%-10d %-20s %-20s \n", (++count), stack.toString(), strToken.substring(cur, strToken.length()));
                } else {
                    ERROR();
                }
            } else if (X.equals("#")) {
                if (X.equals(shuru1)) {
                    flag = false;
                } else {
                    ERROR();
                }
            } else if (productionType()) {

                if (VNTI().equals("")) {
                    ERROR();
                } else if (VNTI().equals("ε")) {
                    stack.pop();
                    System.out.printf("%-10d %-20s %-20s %s->%s\n", (++count), stack.toString(), strToken.substring(cur, strToken.length()), X, VNTI());
                } else {
                    pushStack();
                }
            } else {
                ERROR();
            }
        }
    }

    //出现错误
    private void ERROR() {
        System.out.println("输入串出现错误，无法进行分析");
        System.exit(0);
    }

    //打印存储分析表
    private void printf() {
        if (!flag) {
            System.out.println("****分析成功啦！****");
        } else {
            System.out.println("****分析失败了****");
        }
    }
}