/**
 * Created by lmy on 17-11-27.
 */
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LL {
    private static List<String> prt = new ArrayList<String>();
    private static List<First> first = new ArrayList<First>();
    private static List<First> follow = new ArrayList<First>();
    private static List<TableE> table = new ArrayList<TableE>();

    public static void main(String[] args) {
        LL l = new LL();
        l.read();// 从文件读取文法表达式并且对读到的表达式进行预处理
        System.out.println("读入文法分析分解后为：");
        l.printP();
        l.FindFirstP();
        /*******************************************************
         * 应该加入次数控制，判定，全部first集已经不再发生变化，停*
         * 止调用FindFirstS()函数！*****************************
         * *****************************************************/
        l.FindFirstS();
        l.FindFirstS();
        /*******************************************************/
        l.findFollowP();
        l.findFollowP();
        /*******************************************************/
        System.out.println("first集：");
        l.printF(first);
        System.out.println("follow集：");
        l.printF(follow);
        /*******************************************************/
        l.createTable();// 创建LL(1)表
        l.printT();// 打印表

    }

    public void read() {// 读取文件函数
        BufferedReader br = null;
        String line = "";
        try {
            File file = new File("src/G(E).txt");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileReader reader = new FileReader(file);
            br = new BufferedReader(reader);
            System.out.println("读入的文法是：");
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                pretreatment(line); // 调用预处理方法
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        try {
            if (br != null) {
                br.close();
                br = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void pretreatment(String line) {// 对输入字符进行预处理。分割 形如
        // A->B|a的表达式为A->B和A->a
        Pattern p = Pattern
                .compile("([A-Z]?[']?)([-]?[>]?)([\\w\\W &&[^|]]*)([|]?)");// 重复问题。
        Matcher m = p.matcher(line);
        String emp = "";
        boolean flag = false;

        do {
            flag = false;
            // boolean b=m.find();
            // System.out.println("find "+b);
            if (m.find()) {
                String str = "";
                if (m.group(1).length() > 0 && m.group(2).length() > 0) {
                    emp = m.group(1);
                    str = emp + "->" + m.group(3);// 解决正则表达式问题。第二次寻找时组1和组3有交叉的问题
                } else
                    str = emp + "->" + m.group(1) + m.group(2) + m.group(3);

                // System.out.println(m.group(3)+" "+m.group(4)+" "+m.group(3).length());
                prt.add(str);

                if (m.group(4).length() > 0)
                    flag = true;
                // System.out.println("flag "+flag);
            }

        } while (flag);

    }

    public void printP() {// 打印全部的文法表达式
        Iterator<String> it = prt.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    public void printF(List<First> first) {// 打印全部first或follow集
        Iterator<First> it = first.iterator();
        while (it.hasNext()) {
            First ft = it.next();
            ft.print();
        }
    }

    public void FindFirstP() {// 进行找first集的第一步操作。
        Iterator<String> it = prt.iterator();
        Pattern p = Pattern.compile("([A-Z][']?)(->)([\\w\\W&&[^A-Z]]*)([.]*)");
        while (it.hasNext()) {
            Matcher m = p.matcher(it.next());
            if (m.find() && m.group(3).length() > 0) {
                // System.out.println(m.group(1));
                if (!cmpAdd(m.group(1), m.group(3), first)) {
                    First fc = new First(m.group(1));
                    // System.out.println(m.group(1));
                    fc.addf(m.group(3));
                    first.add(fc);
                }

            }
        }
    }

    public void FindFirstS() {// 进行查找first集的进一步操作。
        Iterator<String> it = prt.iterator();
        while (it.hasNext()) {
            String str = it.next();
            Pattern p = Pattern.compile("([A-Z][']?)(->)([A-Z][']?)");
            Matcher m = p.matcher(str);
            if (str.matches("[A-Z][']?->[[A-Z][']?]+")) {// 如果表达式符合Y->Y1Y2Y3......Yk的形式则执行
                p = Pattern.compile("([A-Z]?[']?)([-]?[>]?)([A-Z][']?)");
                m = p.matcher(str);
                String emp = "";
                boolean flag;
                do {
                    flag = false;
                    if (m.find()) {
                        First fc, ff;
                        if (m.group(1).length() > 0) {
                            emp = m.group(1);
                        }
                        ff = findE(emp, first);
                        if (m.group(3).length() > 0) {
                            fc = findE(m.group(3), first);
                            if (fc.conteinZero()) {// 如果Yi的first集中含有ε，直接将Yi的first集中的内容加到ff中
                                flag = true;
                                ff.addf(fc);
                            } else {// 如果Yi的first集中不含有ε，直接将Yi的first集中的内容加到ff中，并且取出加入的ε
                                ff.addf(fc);
                                ff.reMove('ε');// 因为只要前面的都为有ε即可，最后取到一个可以不含ε

                            }
                        }
                    }
                } while (flag);
            } else if (m.find()) {// 如果表达式不符合Y->Y1Y2Y3......Yk的形式则执行
                // System.out.println(m.group(1)+" "+m.group(3));
                First f1 = findE(m.group(1), first);
                First f2 = findE(m.group(3), first);
                f1.addf(f2.exceptZore());
            }
        }
    }

    private First findE(String name, List<First> first) {// 在找到A的first/follow集中的集合。
        Iterator<First> it = first.iterator();
        First ft = null;
        while (it.hasNext()) {
            ft = it.next();
            if (ft.getName().equals(name)) {
                return ft;
            }
        }
        ft = new First(name);
        first.add(ft);
        return ft;
    }

    private boolean cmpAdd(String name, String str, List<First> first) {// 把新找到的A的First/Follow集中元素加入到其相应的集中。
        Iterator<First> it = first.iterator(); // 加入成功返回true,否则返回false;
        while (it.hasNext()) {
            First ft = it.next();
            if (ft.getName().equals(name)) {
                ft.addf(str);
                return true;
            }
        }
        return false;
    }

    private void findFollowP() {// 创建follow集
        Pattern p = Pattern
                .compile("([A-Z]?[']?)([-]?[>]?)([\\w\\W &&[^A-Z]]*)([[A-Z][']?]*)([\\w\\W &&[^A-Z]]*)");
        Iterator<String> it = prt.iterator();
        Matcher m = null;
        boolean IsFirst = true;
        while (it.hasNext()) {
            String wf = it.next();
            m = p.matcher(wf);
            boolean match = false;

            String ename = "";
            String emp = "";
            do {
                match = m.find();
                if (match) {
                    emp = emp + m.group();// 获取到现在为止获取到的wf中的字符。
                    if (m.group(1).length() > 0) {
                        ename = m.group(1);
                    }
                    // System.out.println(emp+" "+wf.length()+" "+emp.length()+" "+m.group(4));
                    First fc = findE(ename, follow);
                    if (IsFirst) {
                        fc.addf('#');// 文法开始符处理与其他非终结符处理的不同之处。
                        IsFirst = false;
                    }
                    if (wf.length() == emp.length()) { // 将所取到的字符相加得到总字符长度与原字符长度比较即可
                        if (m.group(5).length() > 0) {
                            // System.out.println(m.group(5));
                            String emp5 = m.group(5);
                            String en = "";
                            Pattern p1 = Pattern.compile("([A-Z][']?)");
                            Matcher m1 = p1.matcher(m.group(4));
                            while (m1.find()) {
                                en = m1.group();
                            }
                            if (en.length() > 0) {
                                First f = findE(en, follow);
                                f.addf(emp5);
                            }

                        } else if (m.group(4).length() > 0) {
                            Pattern p1 = Pattern.compile("([A-Z][']?)");
                            Matcher m1 = p1.matcher(m.group(4));
                            List<String> lT1 = new ArrayList<String>();
                            List<String> lT2 = new ArrayList<String>();
                            while (m1.find()) {
                                // System.out.println(emp+" "+m.group(4)+" "+m1.group());
                                lT1.add(m1.group());
                                lT2.add(m1.group());
                            }
                            Iterator<String> it1 = lT1.iterator();
                            while (it1.hasNext()) {
                                String name1 = it1.next();
                                // System.out.println(" "+name1);
                                String name2 = "";
                                Iterator<String> it2 = lT2.iterator();

                                while (it2.hasNext()) {
                                    name2 = it2.next();
                                    // System.out.println(name2+" "+name1);
                                    if (name2.equals(name1))
                                        break;
                                }
                                First flw = findE(name1, follow);
                                boolean flage = true;
                                while (it2.hasNext()) {// 必须是紧紧跟随在E之后的终结符才算follow集中
                                    name2 = it2.next();
                                    // System.out.println(name2+" "+name1);
                                    First fst = findE(name2, first);
                                    flw.addf(fst.exceptZore());
                                    if (!fst.conteinZero()) {
                                        flage = false;
                                        break;// 重要，否则，将会导致程序失败。使如A->BCD形式的表达式，
                                        // 含ε时，仍旧将D的first集中的内容加入到了B的follow集中
                                    }
                                }
                                if (flage) {// 完成A->aBb形式，将A的follow集加到B的follow集中。
                                    First main = findE(ename, follow);
                                    flw.addf(main);
                                }
                            }
                        }
                        match = false;
                    } else if (m.group(5).length() > 0) {
                        // System.out.println(m.group(5));
                        String emp5 = m.group(5);
                        String en = "";
                        Pattern p1 = Pattern.compile("([A-Z][']?)");
                        Matcher m1 = p1.matcher(m.group(4));
                        while (m1.find()) {
                            en = m1.group();
                        }
                        if (en.length() > 0) {
                            First f = findE(en, follow);
                            f.addf(emp5);
                        }
                    }

                }

            } while (match);

        }
    }

    private void createTable() {// 创建ll(1)表
        Iterator<String> it = prt.iterator();
        Pattern p = Pattern.compile("([A-Z][']?)(->)([\\w\\W&&[^A-Z]]?)");
        Matcher m = null;
        while (it.hasNext()) {
            String exp = it.next();
            m = p.matcher(exp);
            boolean b = m.find();
            // System.out.println(b);
            if (b && m.group(1).length() > 0) {
                // System.out.println(m.group(1));
                First ft = this.findE(m.group(1), first);
                if (IsSingle(m.group(1) + m.group(2))) {// 只用m.group(1)时，会造成E与
                    // E'匹配产生错误。丢失表项
                    // System.out.println(m.group(1));
                    String collect = ft.exceptZore().getCollect();
                    int length = collect.length();
                    for (int i = 0; i < length; i++) {
                        TableE t = new TableE(m.group(1), collect.charAt(i),
                                exp);
                        if (!isHas(t))
                            table.add(t);
                    }
                } else if (m.group(3).length() > 0) {
                    // System.out.println(m.group(1)+" "+m.group(3));
                    // First ft=this.findE(m.group(1), first);
                    if (m.group(3).charAt(0) != 'ε') {
                        // System.out.println(m.group(1));
                        if (ft.conteinChar(m.group(3).charAt(0))) {
                            // System.out.println(m.group(1));
                            TableE t = new TableE(m.group(1), m.group(3)
                                    .charAt(0), exp);
                            if (!isHas(t))
                                table.add(t);
                        }
                    }
                }

                if (ft.conteinZero()) {
                    First ff = this.findE(m.group(1), follow);
                    String collect = ff.exceptZore().getCollect();
                    int length = collect.length();
                    for (int i = 0; i < length; i++) {
                        TableE t = new TableE(m.group(1), collect.charAt(i), m
                                .group(1)
                                + "->ε");
                        if (!isHas(t))
                            table.add(t);
                    }
                }

            }
        }
    }

    private boolean isHas(TableE t) {// 判断在表中是不是已经存在了M[A,a],
        Iterator<TableE> it = table.iterator();
        while (it.hasNext()) {
            if (it.next().isEquals(t))
                return true;
        }
        return false;
    }

    private boolean IsSingle(String str) {// 判断表达式 A->a是不是唯一表达式。
        Iterator<String> it = prt.iterator();
        Pattern p = Pattern.compile(str);
        int sign = 0;
        while (it.hasNext()) {
            Matcher m = p.matcher(it.next());
            if (m.find()) {
                sign++;
            }
            if (sign > 1)
                return false;
        }
        return true;
    }

    private void printT() {// 打印整张表格
        Iterator<TableE> it = table.iterator();
        System.out.println("创建的LL(1)分析表：");
        String emp = "";
        while (it.hasNext()) {
            TableE t = it.next();
            String current = t.getNT();
            if (current.equals(emp))
                t.print();
            else {
                System.out.println();
                t.print();
                emp = current;
            }
        }
    }
}