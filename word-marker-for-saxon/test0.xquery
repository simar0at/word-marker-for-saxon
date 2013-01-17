xquery version "1.0";
declare default element namespace "http://www.example.org/test2";
declare namespace shnwm="http://siam.homeunix.net/wordmarker";
declare option saxon:output "method=xml";
declare option saxon:output "indent=yes";

let $validWordChars := "[\u0600-\u06FF]+"
let $allPossibleDelimiters := "[) ]?(?:(?:&amp;gt)|(?:&amp;amp)|(?:[,.%](?!\d))|[-\u2013|()\{\}\[\]'&quot;\u201c\u201d#&amp;/*;:?\u061F!\u060C\s\u200F])+"

for $i in (1,2,3)
return
<test xmlns="http://www.example.org/test">
    {shnwm:markWords("الكاتب العظيم هو اللى بيعتنق مذهب الحريه ، لكن مابيقتصرش على الدعوه للتحرر من الحكام المستبدين بس ، لكن بيدعو كمان للتحرر من التقاليد المستبده.'''", $validWordChars, $allPossibleDelimiters, QName("http://www.example.org/test","w"))}
    <two:anotherTest xmlns:two="http://www.example.org/test2">
    {shnwm:markWords("abc 123", "[a-z]+", "\s+", QName("http://www.example.org/test2","two:w"))}
    </two:anotherTest>
    <someStrangeValues>
    {shnwm:markWords("abc", "[a-z]+", "[.,\s]+", QName("http://www.example.org/test","w"))}
    </someStrangeValues>
</test>