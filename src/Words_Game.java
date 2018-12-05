import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Words_Game {

    public static void main (String args []) {

        // LET'S GO!
        Help.help();
        Player player1 = new Player(Help.set_name_pl(1));
        Player player2 = new Player(Help.set_name_pl(2, player1.getName()));
        Field.show_field_startonly();
        while(!Field.get_status()) { // Event Loop
            Help.set_val(player1);
           // System.out.println(player1.len_of_wds);
            Help.set_val(player2);
           // System.out.println(player2.len_of_wds);
        }
        Help.the_end(player1, player2);

    }

}

class Word_List {
    private static String path_list = "/home/dmitry/IdeaProjects/Words_Game/src/word_rus.txt";

    private static List<String> get_list() {
        List<String> words_list = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path_list))) {
            String str;
            words_list = br.lines().collect(Collectors.toList());

        } catch (IOException e) {
            Handle_Exception.proc_ex(e);
        }

        return words_list;
    }
    static String[] get_arr_list() {
        List<String> words_list = get_list();
        String[] strings = words_list.stream().toArray(String[]::new);

        return strings;
    }

}

class Field {
    private static boolean THE_END = false; // все поля заполнены ??
    private static int size = 6; // размер поля
    private static char[][] FIELD = new char[size+1][size+1]; // создали само поле; +2 - чтобы вывести окантовку поля
    private static char[] alfs = new char[size]; // здесь будут все буквы полей
    private static char def = ' '; // дефолнтно, чем заполняем
    private static String[] alredy_on  = new String [50]; // слова, которые уже есть на поле
    private static String[] words_arr = Word_List.get_arr_list();
    private static int MAX_LEN = get_max_len(); // длина максимального слова в словаре, чтобы не по всему полю бегать например
    private static int count_word = 0; // сколько слов введено уже

    private static void make_field() {
        int i; // счетчик
        // окантовка поля
        int alf = (int)'A';
        int num = (int)'1';

        for (i = 1; i < size+1; i++, alfs[i-2] = (char)alf, alf++)
            FIELD[0][i] = (char)alf;
        for (i = 1; i < size+1; i++, num++)
            FIELD[i][0] = (char)num;
        //_____
        FIELD[0][0] = def;
        for (i = 1; i < FIELD.length; i++) {
            for (int j = 1; j < FIELD[i].length; j++)
                FIELD[i][j] = def;
        }
        String start_word = Random_Word.get_rnd_wrd(size);
        //alredy_on[0] = start_word;
        char[] start_word_ch = start_word.toCharArray();
        int temp = (size%2 == 0) ? (size/2) : ((size/2) + 1); // номер строки
        for (i = 1; i < FIELD.length; i++)
            FIELD[temp][i] = start_word_ch[i-1];
    }
    static void show_field_startonly() { // функция вывода игрового поля
        make_field();
        for (int i = 0; i < FIELD.length; i++) {
            for (int j = 0; j < FIELD[i].length; j++) {
                System.out.print(FIELD[i][j] + "\t");
            }
            System.out.println();
        }
    }
    private static void show_field() {
        for (int i = 0; i < FIELD.length; i++) {
            for (int j = 0; j < FIELD[i].length; j++) {
                System.out.print(FIELD[i][j] + "\t");
            }
            System.out.println();
        }
    }
    static boolean set_field(String field, char w, Player pl) { // когда хотим поставить букву
        boolean resp = false;
        // СНАЧАЛА - ВАЛИДАЦИЯ ПОЛЕЙ, ЧТО ОНИ НЕ ЗАНЯТЫ!!!!!!!! И ТАКИЕ ПОЛЯ СУЩЕСТВУЮТ!!!!
        if (valid_param(field, w)) {
            System.out.println((char) 27 + "[32mВы установили в поле значение! Успешно"+ (char)27 + "[0m");
            // проверка на слова - какой-то метод
            String new_word = check_new_word();
            //System.out.println(new_word);
            if (!new_word.equals(" ")) {
                System.out.println((char)27 + "[32mНа поле появилось новое слово: " + new_word + (char)27 + "[0m");
                alredy_on[count_word] = new_word;
                count_word ++;
                /*for (String str : alredy_on)
                    System.out.println(str);*/
                // добавляем все игроку
                pl.inc_len_count(new_word);
                System.out.println((char)27 + "[34m" + pl.getName() + (char)27 + "[0m, ваша суммарная длина слов на данный момент: "
                        + (char)27 + "[33m" + pl.getLen_of_wds() + (char)27 + "[0m");
            }
            check_end();
            resp = true;
        } else {
            System.out.println((char) 27 + "[31mВсе плохо, ничего не установилось" + (char)27 + "[0m" );
        }
        show_field();
        return resp;
    }
    private static boolean valid_param(String field, char w) {
        boolean resp = false;

        Pattern p = Pattern.compile("^(.)(.)$");
        Matcher m = p.matcher(field);
        if (m.find()) {
            int num_field = Integer.parseInt(m.group(2)); // номер поля - напрмер 5
            char[] alf_field_tmp = m.group(1).toCharArray(); // имя поля - например A
            char alf_field = alf_field_tmp[0];
            int num_alf_field = -1;

            if (num_field <= size && is_contain(alf_field)) {
                // по букве на нужно получить индекс столбца: это какой по счету символ в массиве Alfs + 1
                for (int k = 0; k < alfs.length; k++) {
                    if (alfs[k] == alf_field) {
                        num_alf_field = k + 1;
                        break;
                    }
                }
                // если поле не занято - все хорошо
                if (FIELD[num_field][num_alf_field] == ' ') {
                    // ПРОВЕРИТЬ, ЧТО СВЕРХУ-СНИЗУ или ПОБОКАМ ЕСТЬ ХОТЯ БЫ ОДНО ЗАНЯТОЕ ПОЛЕ
                    if (is_avaliable_toadd(num_field, num_alf_field)) { // если вокруг есть что-то  - добавляем
                        //System.out.println("Yes. Avaliable");
                        FIELD[num_field][num_alf_field] = w;
                        resp = true;
                    } else {
                        System.out.println((char) 27 + "[31mВы пытаетесь добавить букву туда, куда нельзя ее добавить" + (char)27 + "[0m");
                    }

                }


            } else {

            }
        }
        return resp;
    }
    private static boolean is_contain(char w) { //содержится ли такое поле
        boolean resp = false;
        for (char tmp: alfs) {
            if (tmp == w)
                resp  = true;
        }
        if (!resp)
            System.out.println((char) 27 + "[31mТаких полей нет! Попробуйте еще раз" + (char)27 + "[0m");
        return resp;
    }
    private static boolean is_avaliable_toadd (int i, int j) { // содержится ли вокруг этого поля, какие нибудь поля
        boolean resp = false;
        if (i > 1 && i < size) {
            if (j > 1 && j < size) {
                // проверяем все НЕграничные случаи,  то есть все 4 случая!!
                if (FIELD[i-1][j] != def)
                    resp = true;
                if (FIELD[i][j+1] != def)
                    resp = true;
                if (FIELD[i+1][j] != def)
                    resp = true;
                if (FIELD[i][j-1] != def)
                    resp = true;
            } else {
                if (j == 1) {
                    // первый столбец, внутри
                    if (FIELD[i-1][j] != def)
                        resp = true;
                    if (FIELD[i+1][j] != def)
                        resp = true;
                    if (FIELD[i][j+1] != def)
                        resp = true;

                } else {
                    // если последний столбец, внутри
                    if (FIELD[i-1][j] != def)
                        resp = true;
                    if (FIELD[i+1][j] != def)
                        resp = true;
                    if (FIELD[i][j-1] != def)
                        resp = true;
                }
            }
        } else { // если какие то граничные случаи
            if (i == 1) {
                if (j == 1){
                    //System.out.println("Вроде сбда надо зайти...");
                    // левый вверхний угол
                    if (FIELD[i+1][j] != def)
                        resp = true;
                    if (FIELD[i][j+1] != def)
                        resp = true;
                }
                if (j == size) {
                    // правый верхний угол
                    if (FIELD[i+1][j] != def)
                        resp = true;
                    if (FIELD[i][j-1] != def)
                        resp = true;
                }
                if (j > 1 && j< size) {
                    if (FIELD[i+1][j] != def)
                        resp = true;
                    if (FIELD[i][j-1] != def)
                        resp = true;
                    if (FIELD[i][j+1] != def)
                        resp = true;
                }
            } else {
                if (j == 1) {
                    // левый нижний угол
                    if (FIELD[i-1][j] != def)
                        resp = true;
                    if (FIELD[i][j+1] != def)
                        resp = true;
                }
                if (j == size) {
                    // правый нижний угол
                    if (FIELD[i-1][j] != def)
                        resp = true;
                    if (FIELD[i][j-1] != def)
                        resp = true;
                }
                if (j > 1 && j< size) {
                    if (FIELD[i-1][j] != def)
                        resp = true;
                    if (FIELD[i][j-1] != def)
                        resp = true;
                    if (FIELD[i][j+1] != def)
                        resp = true;
                }
            }
        }

        return resp;
    }
    private static void check_end() { // выполняется проверка по всему полю после каждого хода - если заполнилось все поле, то выходим - НО ОНА ПОСЛЕ ПРОВЕРКИ НА СЛОВО
        THE_END = true; // от противного
        for (int i = 1; i < FIELD.length; i ++) {
            for (int j = 1; j < FIELD.length; j++)
                if (FIELD[i][j] == def) {
                    THE_END = false;
                }
        }

    }

    static boolean get_status() {
        return THE_END;
    }
    // а нужно ли это вообще???...
    private static int get_max_len() {
        int max = 0;
        for (String str: words_arr) {
            if (str.length() > max)
                max = str.length();
        }
        return max;
    }
    private static String check_new_word() { // появились ли на поле новые слова
        String new_word = " ";
        char[] temp_ch_ar = new char[MAX_LEN]; // на всякий случай..
        // обнуляем все переменные
        for (char ch: temp_ch_ar)
            ch = ' ';

        boolean flag = true;
        for (int i = 1; i < FIELD.length && flag; i++ ) {
            for (int j = 1; j < FIELD.length && flag; j++) {
                // для каждой точки - обнуляем массив
                for (int a = 0; a < temp_ch_ar.length; a++)
                    temp_ch_ar[a] = ' ';
                if (FIELD[i][j] != def) { // есть ли смысл вообще проверять
                    //System.out.println("Есть, конечно");
                    //System.out.println(FIELD[i][j]);
                    //temp_ch_ar[temp_ch_ar.length] = FIELD[i][j];
                    for (int k = i, tmp = 0; k < FIELD.length; k++, tmp++) {
                        if (FIELD[k][j] != def){
                            //System.out.println("Ну, допустим не равно");
                            temp_ch_ar[tmp] = FIELD[k][j];
                        } else {
                            break;
                        }
                    }
                    if (is_contained_inList(temp_ch_ar)) {
                        // да, есть новое слово
                        String temp_word = new String(temp_ch_ar);

                        Pattern p = Pattern.compile("^([а-яА-Я]+)");
                        Matcher m = p.matcher(temp_word);
                        if (m.find())
                            new_word = m.group(1);
                        //System.out.println(new_word);
                        flag = false;
                    }
                } else {
                    continue;
                }
            }
        }


        return new_word;
    }
    private static boolean is_contained_inList(char[] ch_arr) { // проверяем, если ли такое слово уже
        boolean resp = false;
        String word = new String(ch_arr);
        //System.out.println(word);
        Pattern p = Pattern.compile("^([а-яА-Я]+)");
        Matcher m = p.matcher(word);
        if (m.find()) {
            word = m.group(1);
            //System.out.println(word);
        }
        // проверяем, может такое слово уже есть на поле??
        boolean temp = false;
        try { // NullPointer Exception можем словить. Надо исправить..
            if (alredy_on != null) {
                for (String str : alredy_on) {
                    if (!str.isEmpty() & str.equals(word)) {
                        temp = true;
                        break;
                    }

                }
            }
        } catch (Exception e) {
            Handle_Exception.proc_ex(e);
        }

        if (!temp) {
            for (String str : words_arr) {
                if (str.equals(word)) {
                    //System.out.println("ДА!");
                    resp = true;
                    break;
                }
            }
        }


        return resp;
    }


}


class Random_Word {
    static String get_rnd_wrd (int len) { // метод, возвращающий рандомное слово из списка
        String[] strings = Word_List.get_arr_list();
        String rnd_word = " ";

        Random random = new Random();
        int num = 1 + random.nextInt(2000 - 1);
        int count = 0;
        try { // если вдруг слов с заданной длиной не будет совсем, то - выбросится исключение
            for(String word : strings) {
                if (word.length() == len)
                    count++;
                if (count == num) {
                    rnd_word = word;
                    break;
                }
            }

        } catch (Exception e) {
            Handle_Exception.proc_ex(e);
        }

        return rnd_word;
    }
}

class Player {
    private String name;
    private int count_of_wds = 0;
    private int len_of_wds = 0;

    Player(String name) {
        this.name = name;
    }

    void inc_len_count(String cur_word) {
        this.len_of_wds += cur_word.length();
        this.count_of_wds ++;
    }
    int getCount_of_wds() {
        return this.count_of_wds;
    }
    int getLen_of_wds() {
        return this.len_of_wds;
    }
    String getName() { return this.name;}


}
// класс - обработчик ошибок
class Handle_Exception {
    private static String path_err = "/home/dmitry/IdeaProjects/Words_Game/src/log_errors.txt"; // путь файла с логом ошибок

    static void proc_ex(Exception e) {
        Date date = new Date();
        try {
            FileWriter fstream = new FileWriter(path_err, true);
            BufferedWriter out = new BufferedWriter(fstream);
            PrintWriter pWriter = new PrintWriter(out, true);
            pWriter.write(date.toString()+"\n");
            //e.printStackTrace(pWriter);
            pWriter.write(e.toString());
            pWriter.write("\n____________________\n");
            // закрываем потоки
            pWriter.close();
            fstream.close();
            out.close();
        }
        catch (Exception ie) {
            throw new RuntimeException("Could not write Exception to file", ie);
        }
    }
}

class Help {
    private static String path_help = "/home/dmitry/IdeaProjects/Words_Game/src/rules.txt";
    private static Scanner scan = new Scanner(System.in);

    static void help() {
        try(FileReader fr = new FileReader(path_help)) {
            Scanner scan = new Scanner(fr);
            while (scan.hasNextLine()) {
                System.out.println((char)27 + "[33m" + scan.nextLine() + (char)27 + "[0m");
            }
            System.out.println((char)27 + "[34mИГРА НАЧАЛАСЬ!" + (char)27 + "[0m\r\n");
        } catch (Exception e) {
            Handle_Exception.proc_ex(e);
        }
    }
    static String set_name_pl(int num) {
        String name = " ";
        System.out.print("Введите имя игрока " + num + " (не более 10 символов): ");

        name = scan.nextLine();
        if (name.length() > 10) {
            System.out.println((char)27 + "[31mВы ввели слишком длинное имя. Попробуйте еще раз ;)" + (char)27 + "[0m");
            set_name_pl(num);
        }

        return name;
    }
    static String set_name_pl(int num, String name_1) {
        String name = " ";
        System.out.print("Введите имя игрока " + num + " (не более 10 символов): ");

        name = scan.nextLine();
        if (name.length() > 10) {
            System.out.println((char)27 + "[31mВы ввели слишком длинное имя. Попробуйте еще раз ;)" + (char)27 + "[0m");
            set_name_pl(num);
        }
        if (name.equals(name_1)) { // если имя второго игрока такое же как у первого - все плохо
            System.out.println((char)27 + "[31mВы ввели такое же имя, как и у первого игрока. Попробуйте еще раз ;)"
                    + (char)27 + "[0m");
            set_name_pl(num, name_1);
        }

        return name;
    }
    static void set_val(Player pl) {
        String name_pl = pl.getName();
        String param;
        String fld; // поле, куда хотим ввести
        String w_str; // какую букву хотим ввести
        char w;

        System.out.print((char)27 + "[34m" + name_pl + (char)27 +
                "[0m, пожалуйста, выберите поле, в которое вы хотите поставить букву (через пробел): ");
        param = scan.nextLine();
        Pattern p = Pattern.compile("(\\w{2})(?:\\s+)([а-яА-Я])");
        Matcher m = p.matcher(param);
        if (!m.find()) {
            System.out.println("Это что за набор символов? А ну-ка еще раз попробуй давай: ");
            set_val(pl);
        } else {
            // разбираем параметры
            fld = m.group(1);
            w_str = m.group(2);
            // проверяем параметры
            if (!valid_param(fld,w_str)) {
                System.out.println("Ошибка в самих параметрах. А ну-ка еще раз попробуй давай: ");
                set_val(pl);
            }
            char[] tmp = w_str.toCharArray();
            w = tmp[0];
            // устанавливаем значение в поле
            if (!Field.set_field(fld,w,pl)) {
                set_val(pl);
            }
        }

    }
    private static boolean valid_param(String fld, String w) {
        Pattern p1 = Pattern.compile("^[A-Z]\\d$");
        Pattern p2 = Pattern.compile("^[а-яА-Я]$");
        Matcher m1 = p1.matcher(fld);
        Matcher m2 = p2.matcher(w);
        if (m1.find() && m2.find()) { return true; }
        else {
            if (!m1.find())
                System.out.println("Что-то не так с выбранным полем..");
            if (!m2.find())
                System.out.println("Что-то не так с добавляемым значением..");

            return false;
        }
    }

    static void the_end(Player pl1, Player pl2) {
        // проверка, кто победил...
        System.out.println((char)27 + "[33mСпасибо. Игра закончена, победил игрок: " + (char)27 + "[0m");
    }
}