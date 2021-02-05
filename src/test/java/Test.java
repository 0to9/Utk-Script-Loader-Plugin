import java.util.Calendar;

public class Test {
    public static void main(String[] args) {
        updateTimeStamp();
    }

    public static void updateTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        String date = pad(calendar.get(Calendar.YEAR)) + pad(calendar.get(Calendar.MONTH)) + pad(calendar.get(Calendar.DATE));
        String time = pad(calendar.get(Calendar.AM_PM) * 12 + calendar.get(Calendar.HOUR)) + pad(calendar.get(Calendar.MINUTE)) +
                pad(calendar.get(Calendar.SECOND)) + pad2(calendar.get(Calendar.MILLISECOND));
        String readable = calendar.getTime().toString().replaceAll(":", ".");

        System.out.println(date + time + " - " + readable);
    }

    private static String pad(int i) {
        return (i < 10 ? "0" : "") + i;
    }
    private static String pad2(int i) {
        return (i < 10 ? "00" : i < 100 ? "0" : "") + i;
    }
}
