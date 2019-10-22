package cn.shiyujun.hystrix;

/**
 * d
 *
 * @author syj
 * CreateTime 2019/10/21
 * describe:
 */
public class CommandTest {
    public static void main(String[] args) {
        HelloCommand command = new HelloCommand();

        String result = command.execute();
        System.out.println(result);
    }
}
