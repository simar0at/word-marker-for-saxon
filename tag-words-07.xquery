xquery version "1.0";
declare default element namespace "http://www.mediawiki.org/xml/export-0.8/";
declare namespace ex07="http://www.mediawiki.org/xml/export-0.7/";
declare namespace swc="http://sweble.org/doc/site/tooling/sweble/sweble-wikitext";
declare namespace ptk="http://sweble.org/doc/site/tooling/parser-toolkit/ptk-xml-tools";
declare namespace functx = "http://www.functx.com"; 
declare namespace shnwm = "http://siam.homeunix.net/wordmarker";
declare boundary-space preserve;
declare option saxon:output "method=xml"; 

(: Based upon XQuery by Priscilla Walmsley (c) O'Reilly Media 2007 :)
(: Example 9-6. Useful function: change-elem-names :)
(: See also: http://www.xqueryfunctions.com :)

declare function local:change-elem-names-keep-named
  ($nodes as node()*, $old-names as xs:string+,
   $new-names as xs:string+, $suppress-tags as xs:string*, $print-tags as xs:string*, $return-value as xs:boolean) as node()* {

  if (count($old-names) != count($new-names))
  then error(xs:QName("Different_Number_Of_Names"))
  else for $node in $nodes
       return if ($node instance of element() or $node instance of document-node())
              then let $newName :=
                     if (name($node) = $old-names)
                     then $new-names[index-of($old-names, name($node))]
                     else ()
                   return
                     if (name($node) = $suppress-tags) then ()
                     else if (empty($newName))
                     then local:change-elem-names-keep-named($node/node(),
                                              $old-names, $new-names, $suppress-tags, $print-tags, name($node) = $print-tags)
                     else
                       let $newContent := local:change-elem-names-keep-named($node/node(),
                                              $old-names, $new-names, $suppress-tags, $print-tags, name($node) = $print-tags)
                       return
                         if (empty($newContent)) then () (: if processing removed all content, remove the node itself :)
                         else element {$newName}
                     {$node/@*,
                      $newContent}
              else if ($return-value) then text{(" ", $node)}
                else ()
};

declare function local:change-elem-names-keep-named
  ($nodes as node()*, $old-names as xs:string+,
   $new-names as xs:string+, $suppress-tags as xs:string*, $print-tags as xs:string*) as node()* {
   local:change-elem-names-keep-named($nodes,                                              $old-names, $new-names, $suppress-tags, $print-tags, false()) };
                                              
declare function local:normalize-space-values($nodes as node()*) {
  for $node in $nodes
     return if ($node instance of element() or $node instance of document-node())
        then 
          let $newContent := local:normalize-space-values($node/node())
          return if (empty($newContent)) then () (: if processing removed all content, remove the node itself :)
          else   
            element {name($node)}
                     {$node/@*,
                      $newContent, ""}
        else
          if (matches($node, "^\s+$")) then () (: whitespace only is meaningless :)
          else normalize-space($node)
};

declare function local:tag-words
   ( $nodes as node()* ) as node()* {
let $validWordChars := "[\u0600-\u06FF]+"
let $allPossibleDelimiters := "[) ]?(?:(?:&amp;lt)|(?:&amp;gt)|(?:&amp;amp)|(?:[,.%](?!\d))|[-\u06D4\u2013\u2014=|()\{\}\[\]<>\u27E8\u27E9'\u2018\u2019&quot;\u00ab\u00bb\u2039\u203A\u201c\u201d#&amp;/*\u2022;:?\u061F!\u060C\s\u200F\u202E\u202C\u200D])+"
for $node in $nodes
  return if ($node instance of element() or $node instance of document-node())
         then element {name($node)}
              {$node/@*,
               local:tag-words($node/node())}
         else
             shnwm:markWords($node, $validWordChars, $allPossibleDelimiters, QName("http://www.mediawiki.org/xml/export-0.8/", "w"))
 };
 
 declare function local:get-sentences
   ( $nodes as node()* ) as node()* {
for $node in $nodes
  return if ($node instance of element() or $node instance of document-node())
         then element {name($node)}
              {$node/@*,
               local:get-sentences($node/node())}
         else
             local:tag-at-pattern-preserve-space($node, "([^د])[.?&#x061F;!]([^\d])", "s")
 };
 
declare function local:tag-at-pattern-preserve-space
  ( $string as xs:string? ,
    $regex as xs:string,
    $tag as xs:string)  as node()* {
    
    let $iomf := functx:index-of-match-first($string, $regex)
    return
       if (empty($iomf))
       then <s xml:space="preserve">{$string}</s>
       else
           let $length := 
            string-length($string) -
            string-length(local:replace-first-limited($string, $regex,''))
           return 
           (element {$tag}
                    { attribute {"xml:space"} {"preserve"},
                      substring($string,1,$iomf + $length)},
                 local:tag-at-pattern-preserve-space(
                     substring($string,$iomf + $length + 1), $regex, $tag))
  };
  
  declare function functx:index-of-match-first 
  ( $arg as xs:string? ,
    $pattern as xs:string )  as xs:integer? {
       
  if (matches($arg,$pattern))
  then string-length(tokenize($arg, $pattern)[1]) + 1
  else ()
 } ;
 
(: Adds matching parts of the pattern which are used to exclude some possible matches :)
declare function local:replace-first-limited 
  ( $arg as xs:string? ,
    $pattern as xs:string ,
    $replacement as xs:string )  as xs:string {
       
   replace($arg, concat('(^.*?)', $pattern),
             concat('$1',$replacement, '$2$3$4$5$6$7$8$9'))
 } ;

let $baseConfig := swc:configureSiteFromURL(/ex07:mediawiki/ex07:siteinfo/ex07:sitename,
    /ex07:mediawiki/ex07:siteinfo/ex07:base, true(), false())    

(: do not use namespace/text() because that will skip the default namespace with key 0 !:)
let $config := swc:configureNamespace(data(/ex07:mediawiki/ex07:siteinfo/ex07:namespaces/ex07:namespace/@key), /ex07:mediawiki/ex07:siteinfo/ex07:namespaces/ex07:namespace, $baseConfig)

let $pageTitles := (
for $page in /ex07:mediawiki/ex07:page
where $page/ex07:ns != 10
return
  swc:storePageTitle($page/ex07:title, $page/ex07:revision[1]/ex07:id, $config)
)

let $templates := $pageTitles|(
for $page in /ex07:mediawiki/ex07:page
where $page/ex07:ns = 10
return
  swc:storeTemplate($page/ex07:title, $page/ex07:revision[1]/ex07:id, $page/ex07:revision/ex07:text[not($page/ex07:title = ())], $config)
)

return
<mediawiki xmlns="http://www.mediawiki.org/xml/export-0.8/"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xmlns:ptk="http://sweble.org/doc/site/tooling/parser-toolkit/ptk-xml-tools" xmlns:swc="http://sweble.org/doc/site/tooling/sweble/sweble-wikitext" version="0.8" xml:lang="arz">
<siteinfo>
    <sitename>{/ex07:mediawiki/ex07:siteinfo/ex07:sitename/text()}</sitename>
    <base>{/ex07:mediawiki/ex07:siteinfo/ex07:base/text()}</base>
    <generator>{concat(/ex07:mediawiki/ex07:siteinfo/ex07:generator/text(), "+tag-words-07.xquery")}</generator>
    <namespaces>
        {/ex07:mediawiki/ex07:siteinfo/ex07:namespaces/ex07:namespace[@key = 0]}
    </namespaces>
</siteinfo>
    {
(: Limited processing for test purpose :)
(:for $page in subsequence(//ex07:page, 10, 30):)
(: Process all pages :)
for $page in //ex07:page

let $title := $page/ex07:title/text()

let $contributorUsername := $page/ex07:revision[1]/ex07:contributor/ex07:username/text()

let $pageParsed := $templates|swc:parseMediaWiki($title, $page/ex07:revision/ex07:text/text(), $config)

let $textsOnly := local:change-elem-names-keep-named($pageParsed, ("swc:WtParagraph", "swc:WtHeading", "swc:WtTableImplicitTableBody", "swc:WtImageLink", "swc:WtListItem", "swc:WtDefinitionListDef", "swc:WtDefinitionListTerm"), ("p", "h", "p", "p", "p", "p", "p"), ("swc:WtXmlAttribute", "swc:WtExternalLink"), ("ptk:t"))

let $cleanedTexts := <text><h>{$title}</h>{local:normalize-space-values($textsOnly)}</text>
let $sentencesTagged := local:tag-words(local:get-sentences($cleanedTexts))
(:process only articles started by a BOT:)
(:where  ends-with(upper-case($contributorUsername), "BOT"):)
(:process only articles having some issues:)
(:where $title = "اسرائيل":)
(:where $title = "امريكا الشماليه":)
(:where $title = "اندريه اجاسى":)
(:where $title = "رسم قلب":)
(:where $title = "مصر":)
(:where $title = "المماليك البحريه":)
(:where $title = "عمرو دياب":)
(:if not using any other where-clauses use this one. Processung other namespaces is not useful:)
where $page/ex07:ns = 0
return
    <page>
      <title>{$title}</title>
      <ns>{$page/ex07:ns/text()}</ns>
      <id>{$page/ex07:id/text()}</id>
      <revision>
        <id>{$page/ex07:revision[1]/ex07:id/text()}</id>
        <parentid>{$page/ex07:revision[1]/ex07:parentid/text()}</parentid>
        <timestamp>{$page/ex07:revision[1]/ex07:timestamp/text()}</timestamp>
        <contributor>{$page/ex07:revision[1]/ex07:contributor/text()}</contributor>
        <comment>{$page/ex07:revision[1]/ex07:comment/text()}</comment>
          {$sentencesTagged}
        <!-- This is new in version 0.8, just generate a default. -->
        <model>wikitext</model>
        <format>text/x-wiki</format>
      </revision>
    </page>
}
</mediawiki>