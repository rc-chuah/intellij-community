// "Replace with 'getOrDefault' method call" "false"
import java.util.Map;

public class Main {
  private String str;

  public void testGetOrDefault(Map<String, String> map, String key, Main other) {
    str = map.get(k<caret>ey);
    if(str == null) {
      // comment
      other.str = "";
    }
    System.out.println(str);
  }
}