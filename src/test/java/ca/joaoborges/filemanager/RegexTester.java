package ca.joaoborges.filemanager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTester {

    public static void main(final String[] args) {
        final Matcher matcher = Pattern.compile("\\(.*\\).*")
            .matcher("(uohaoudh) david guetta vs. calvin harris vs. afrojack - ain't a party vs. c.u.b.a. vs. hey mama ");

        System.out.println(matcher.group());
    }

}
