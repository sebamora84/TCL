package com.mlstuff.apps.tcl;

import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Created by Diego on 8/19/2015.
 */
public class WebReader {

    public static LinkedList<TclItem> ParseAllTclItems(String mainHTML)
    {
        LinkedList<TclItem> itemList = new LinkedList<TclItem>();

        String[] linkElements = mainHTML.split(Pattern.quote("<h3><a href=\""));
        String[] imageElements = mainHTML.split(Pattern.quote("class=\"e\"><img src=\""));

        for (int i = 1; i < linkElements.length; i++)
        {
            TclItem item = new TclItem();
            String urlTitle = linkElements[i].split(Pattern.quote("</a></h3>"))[0];

            String[] splitUrlTitle =  urlTitle.split("\">");
            item.ItemUrl = splitUrlTitle[0];
            item.Title  = splitUrlTitle[1];
            item.ImageUrl  = imageElements[i].split(Pattern.quote("\"><br><i>"))[0];
            itemList.add(item);
        }
        return itemList;
    }

    public static TclItem GetSingleTclItems(String mainHTML)
    {

        String linkElement = mainHTML
                .split(Pattern.quote("<div class=\"centre\"> <h3>"),2)[1]
                .split(Pattern.quote("</i></p> </div>  </div>"),2)[0];

        String title = linkElement
                .split(Pattern.quote("</h3> </div>"), 2)[0];

        String imageUrl = linkElement
                .split(Pattern.quote("<p class=\"e\"><img src=\""), 2)[1]
                .split(Pattern.quote("\"><br><i>"), 2)[0];

        TclItem item = new TclItem();

        item.ImageUrl = imageUrl;
        item.Title  = title;

        return item;
    }
}

