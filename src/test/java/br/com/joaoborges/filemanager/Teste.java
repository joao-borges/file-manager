package br.com.joaoborges.filemanager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * N/A
 *
 * @author JoãoGabriel
 */
public class Teste {

	public static void main(String[] args) throws Exception {
		Matcher m = Pattern.compile("\\(.*\\).*").matcher("(uohaoudh) david guetta vs. calvin harris vs. afrojack - ain't a party vs. c.u.b.a. vs. hey mama ");

		System.out.println(m.group());

	}
}
