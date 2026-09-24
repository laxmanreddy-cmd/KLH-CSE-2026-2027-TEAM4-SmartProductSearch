import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        File myObj = new File("Output.txt");
        Scanner sc = new Scanner(System.in);
        boolean flag = false;

        System.out.print("Enter the word to be searched: ");
        String key = sc.next();

        try {
            Scanner myReader = new Scanner(myObj);

            while (myReader.hasNextLine()) {

                String data = myReader.nextLine();
                String[] words = data.split(" ");

                for (String word : words) {
                    if (word.equalsIgnoreCase(key)) {
                        flag = true;
                        break;
                    }
                }

                if (flag)
                    break;
            }

            myReader.close();

            if (flag)
                System.out.println("Word Found");
            else
                System.out.println("No Word Found");

        } catch (Exception e) {
            System.out.println("An error occurred.");
        }

        sc.close();
    }
}