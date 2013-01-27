xquery version "1.0";
(:declare default element namespace "http://www.mediawiki.org/xml/export-0.7/";:)
declare default element namespace "http://www.mediawiki.org/xml/export-0.8/";
declare boundary-space strip;
declare option saxon:output "method=text";

(: In principle
string-join(//text, " ")
would do but there are many useless newlines and whitepaces one can get rid of. :)

(: Compact the whitspaces, separate every tagged entity by space and every article by a newline :)
string-join(
for $text in //text/*
    return
        string-join(normalize-space($text), " ")
, "&#10;")