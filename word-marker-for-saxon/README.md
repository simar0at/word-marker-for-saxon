Surround words with tags using XQuery and Saxon
===============================================

This is a little demo of Saxon extension functions,
but it serves a real purpose in my diploma thesis project.
It surrounds everything that does not matches a configurable
Java regular expression with a tag you provide. 
The bundled swc-saxon-interface is just used for conveniently
both extension functions at once. It should be replaced
with a more current version if available. The jar file
is ready for standalone use or combined with swc-saxon-interface
depending on what initialization function/object is used.
This software uses a tweaked version of the OpenJDK 6 scanner class.

Example
-------

The software packaged together with the source is available as ready made
JAR in this directory.
If you want to look at what this does run Saxon HE
(or another edition if you like) net.sf.saxon.Query with the parameters
-init:net.homeunix.siam.wordmarker.AddWordMarkerFunction -q:test0.xquery
Or configure this as an Extension in <oXygen/> XML editor.

Legal stuff
-----------

This code is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License version 2 only, as
published by the Free Software Foundation and the Classpath Exception
by Oracle.
