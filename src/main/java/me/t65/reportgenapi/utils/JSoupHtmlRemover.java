package me.t65.reportgenapi.utils;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
public class JSoupHtmlRemover implements HtmlRemover {

    @Override
    public String unescapeAndRemoveHtml(String content) {
        String escapedContent = StringEscapeUtils.unescapeHtml4(content);
        return Jsoup.parse(escapedContent).text().trim();
    }
}
