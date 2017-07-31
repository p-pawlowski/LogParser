
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LogParser {

	public static void main(String[] args) {

		Scanner scan = new Scanner(System.in);
		System.out.println("Wprowadź ścieżkę do folderu w którym znajdują się logi");
		String url = scan.nextLine();
		url = url.replace("\\", "/");
		System.out.println("Wprowadź wyrażenie regularne na podstawie którego chcesz analizować logi:");
		String regex = scan.nextLine();
		LocalDateTime now = LocalDateTime.now();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
		String formatDateTime = now.format(formatter);
		String fileName = "log_" + formatDateTime;

		Path path = Paths.get(fileName);
		try {
			Files.write(path, new ArrayList<String>());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			
			LogParser.listFilesForFolder(regex, url, path);
		} catch (IOException e1) {
			System.out.println("--- Wprowadziłeś nieistniejącą ścieżkę folderu ---");
			return;
		}
		
		System.out.println("--- zapisano plik z logiem: " + fileName + " ---");
		scan.close();

	}

	static void parseFile(String url, String regex, Path path) throws IOException {

		List<String> logList = new ArrayList<>();
		Pattern pattern = Pattern.compile(regex);

		ZipFile zipFile;

		zipFile = new ZipFile(url);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			InputStream stream = zipFile.getInputStream(entry);
			Scanner scan = new Scanner(stream, "UTF-8");

			while (scan.hasNextLine()) {
				String str = scan.nextLine();
				Matcher matcher = pattern.matcher(str);
				if (matcher.find()) {
					logList.add("+++ " + entry.getName() + " +++ - " + str);
				}
			}
		}

		
			Files.write(path, logList, StandardOpenOption.APPEND);
		
		

	}

	static public void listFilesForFolder ( String regex, String uri, Path path1) throws IOException {
		List<Path> filesInFolder = Files.walk(Paths.get(uri)).filter(Files::isRegularFile)
				// .map(Path::toFile)
				.collect(Collectors.toList());
		for (Path path : filesInFolder) {
			
				List<String> list = new ArrayList();
				list.add("\n" + "----- " + path.getFileName() + " -----");
				Files.write(path1, list, StandardOpenOption.APPEND);

				String url = uri + path.getFileName();
				LogParser.parseFile(url, regex, path1);
			
		}
	}
}