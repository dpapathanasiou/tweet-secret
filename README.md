tweet-secret
============

About
-----

This is a text <a href="https://en.wikipedia.org/wiki/Steganography" target="_blank">steganography</a> application optimized for use on <a href="https://twitter.com/" target="_blank">Twitter</a>, written in <a href="http://clojure.org/" target="_blank">Clojure</a>.

This is also my entry in the 2013 <a href="http://lispinsummerprojects.org/" target="_blank">Lisp in Summer Projects</a> contest. 

Background and Motivation
-------------------------

Twitter accounts are either completely private or public; if you have a public account and want to tweet something privately to a select list of people, there is no simple way to do it.

Twitter does have a <a href="https://support.twitter.com/articles/14606-posting-or-deleting-direct-messages" target="_blank">direct message feature</a>, but it has several drawbacks: you can only send messages to people one at a time, you're limited to people who follow you explicitly, and worst of all, it may be subject to 3rd party snooping, governmental or otherwise.

How It Works
------------

The tweet-secret application is essentially a <a href="https://en.wikipedia.org/wiki/Book_cipher" target="_blank">book cipher</a>, using plain text files available on the web or commonly-shared among a group of people as the "book" or corpus text. The corpus, which consists of one or more texts, is known only among the sender and intended recipients, and should be changed frequently to prevent eavesdroppers from picking up the pattern or method.

The corpus text is parsed into a list of sentences, filtered by character length to exclude anything longer than 140 characters in size. This list of sentences is known as the set of eligible tweets.

Messages are encoded by looking up each word token in a common dictionary, then using that pointer reference (sequence in one or more dictionary lists) to a sentence in the set of eligible tweets whose preceeding cumulative string length matches the pointer. That sentence becomes the tweet to be broadcast, to correspond to the word token.

Since the exact pointer position may not match the start or end of a corpus sentence exactly, an unobtrusive marker is used at that point in the text (by default a <a href="http://www.fileformat.info/info/unicode/char/00b7/index.htm" target="_blank">unicode middle dot</a> character, which can be changed in the config.properties file), which is important for decoding.

Tweets are decoded by finding their position in the set of eligible tweets, counting the cumulative string size up to that point, and adding the amount of the offset marker, if present. The resulting number is the dictionary pointer, which is used to lookup the corresponding word.

Installation
------------

Get and install <a href="http://leiningen.org/" target="_blank">Leiningen</a> if you do not already have it.

Next, clone this repo to your computer, go to the folder where it exists, and run the following commands from a terminal:

```
$ lein deps
$ lein uberjar
```

If the build succeeds, you should now have a jar file created in the repo's target folder named <tt>tweet-secret-1.0-standalone.jar</tt>.

Usage
-----

The current version works via the command line. Use the <tt>--help</tt> switch when invoking the standalone jar file to get the list of options:

```
$ java -jar tweet-secret-1.0-standalone.jar --help
tweet-secret: Text steganography optimized for Twitter

 Switches               Default  Desc                                                                                                             
 --------               -------  ----                                                                                                             
 -c, --corpus                    REQUIRED: at least one url or full path filename of the secret corpus text(s) known only by you and your friends 
 -d, --decode                    Decode this tweet into plaintext (if none present, text after all the option switches will be encoded)           
 -h, --no-help, --help  false    Show the command line usage help                                                                                 

```

### Encoding

### Decoding

Examples
--------

Suppose we want to encode the message "<b>Tonight we take Paris by storm</b>" as a series of innocuous-looking tweets. 

Let's use <a href="http://textfiles.com/etext/NONFICTION/mexico" target="_blank">The History Of The Conquest Of Mexico</a> by William Hickling Prescott on textfiles.com (<a href="http://textfiles.com/etext/NONFICTION/mexico" target="_blank">http://textfiles.com/etext/NONFICTION/mexico</a>) as the randomly selected corpus text.

Again, the corpus text is known only by us and the followers we want to be able to read the message. The corpus text should be changed frequently, and ideally, never used twice, ever.

Open a terminal, go to the target folder containing the standalone jar file, and type this command:

```
$ java -jar tweet-secret-1.0-standalone.jar --corpus http://textfiles.com/etext/NONFICTION/mexico \
"Tonight we take Paris by storm"
```

On Mac OSX, you should also include <tt>-Dfile.encoding=utf-8</tt> as a command line argument to the java interpreter so that the tweet strings are output correctly: 

```
$ java -Dfile.encoding=utf-8 -jar tweet-secret-1.0-standalone.jar --corpus http://textfiles.com/etext/NONFICTION/mexico \
"Tonight we take Paris by storm"
```

This results in the following six tweets, one for each word of the original message:

```
On the following morning, the gener·al requested permission to return the emperor's visit, by waiting on him in his palace.
A pitched battle follow·ed.
But the pride of Iztapalapan, on which its lord had freely l·avished his care and his revenues, was its celebrated gardens.
This form of governm·ent, so different from that of the surrounding nations, subsisted till the arrival of the Spaniards.
The Mexica·ns furnish no exception to this remark.
He ·felt his empire melting away like a morning mist.
```

Followers who know the corpus text can decode these tweets with this command:

```
$ java -jar tweet-secret-1.0-standalone.jar --corpus http://textfiles.com/etext/NONFICTION/mexico \
--decode "On the following morning, the gener·al requested permission to return the emperor's visit, by waiting on him in his palace." \
--decode "A pitched battle follow·ed." \
--decode "But the pride of Iztapalapan, on which its lord had freely l·avished his care and his revenues, was its celebrated gardens." \
--decode "This form of governm·ent, so different from that of the surrounding nations, subsisted till the arrival of the Spaniards." \
--decode "The Mexica·ns furnish no exception to this remark." \
--decode "He ·felt his empire melting away like a morning mist." 
```

Which results in this list of words, corresponding to the original message:

```
tonight
we
take
Paris
by
storm
```

The encoding can also be done with multiple corpus texts, mixing urls and plain text files available on your computer's filesystem:

```
$ java -jar tweet-secret-1.0-standalone.jar --corpus http://textfiles.com/humor/1988.hilite \
--corpus /home/guest/Downloads/random-text.txt \
--corpus http://textfiles.com/humor/att.txt \
--corpus http://textfiles.com/humor/contract.moo \
--corpus http://www.gutenberg.org/cache/epub/1232/pg1232.txt \
--corpus http://textfiles.com/humor/collected_quotes.txt \
--corpus http://www.gutenberg.org/cache/epub/74/pg74.txt \
--corpus /home/guest/Downloads/english.txt \
--corpus http://www.gutenberg.org/cache/epub/844/pg844.txt \
"Tonight we take Paris by storm"
```

The only caveat with using local files such as these is that your followers (i.e., people who decode the tweets) must have the same exact files on their computers.


