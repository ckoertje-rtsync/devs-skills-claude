package Models.java;

import java.util.ArrayList;
import java.util.List;

public class Display {

	private static boolean enabled = true;

	public static void setEnabled(boolean flag) {
		enabled = flag;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	 public static void PhaseString(String text) {
		 PhaseString(text, 70);
	 }
    public static void PhaseString(String text,int openTime) {
        if (!enabled) return;
        new Thread(() -> {
            try {
            List<String> lines = wrapLines(text, 50);
            String os = System.getProperty("os.name").toLowerCase();

            List<String> cmd = new ArrayList<>();

            if (os.contains("win")) {
                // Windows: start a new cmd window, echo lines, wait 10s
                StringBuilder sb = new StringBuilder();
                for (String line : lines) {
                    if (sb.length() > 0) sb.append(" & ");
                    sb.append("echo ").append(line);
                }
                sb.append(" & timeout /t 10 >nul");

                cmd.add("cmd.exe");
                cmd.add("/c");
                cmd.add("start");
                cmd.add("cmd.exe");
                cmd.add("/c");
                cmd.add(sb.toString());

            } else {
                // Linux/macOS: xterm, echo lines, sleep       cmd.add("/c");  
                StringBuilder sb = new StringBuilder();
                for (String line : lines) {
                    sb.append("echo '")
                      .append(line.replace("'", "'\\''"))
                      .append("'; ");
                }
                sb.append("sleep "+  openTime  );

                cmd.add("sh");
                cmd.add("-c");
                cmd.add("xterm -hold -e \"" + sb.toString() + "\"");

            }

         
            ProcessBuilder pb =  new ProcessBuilder(cmd);
            
            Process process = pb.start();  

            Thread.sleep(openTime);

            System.out.println("Closing viewer for: ");
            process.destroy();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }).start();
    }


 // Utility: wrap text into lines of max width
    public static List<String> wrapLines(String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String word : text.split(" ")) {
            if (current.length() + word.length() + 1 > width) {
                lines.add(current.toString());
                current = new StringBuilder();
            }
            current.append(word).append(" ");
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    public static void Image(String filename) {
        Image(filename, "C:\\Users\\berni\\Downloads\\");
    }
    public static void Image(String filename, String pathToImage) {
        if (!enabled) return;
        new Thread(() -> {
            try {
                String viewer = "C:\\Program Files\\IrfanView\\i_view64.exe";

                ProcessBuilder pb = new ProcessBuilder(viewer, pathToImage+filename);
              
                Process process = pb.start();

     
                Thread.sleep(2000);

                System.out.println("Closing viewer for: " + filename);
                process.destroy();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
	public static void main(String[] args) {
           PhaseString("Hello",70);

	}

}
