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

Since the exact pointer position may not match the start or end of a corpus sentence exactly, an unobtrusive marker is used at that point in the text (by default a <a href="http://www.fileformat.info/info/unicode/char/00b7/index.htm" target="_blank">unicode middle dot</a> character, which can be changed in the [config.properties file](https://github.com/dpapathanasiou/tweet-secret/blob/master/config.properties)), which is important for decoding.

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

### Testing the Installation

There are two test cases, containing four assertions, included which test the encoding and decoding of an English-language message, using a [static corpus from gutenberg.org](http://www.gutenberg.org/cache/epub/1661/pg1661.txt). 

The dictionary for testing is re-bound to a [static, universally available dictionary text](http://www.cs.duke.edu/~ola/ap/linuxwords) found online, since the linux words file as defined by default in [config.properties](https://github.com/dpapathanasiou/tweet-secret/blob/master/config.properties) can vary from distro to distro and computer to computer.

To run the tests, use this command (you will need an internet connection to have them run successfully, since both the corpus and dictionary texts used in the texts are defined as remote URLs):

```
$ lein test
```

If successful, you should see this:

```

lein test tweet-secret.core-test

Ran 2 tests containing 4 assertions.
0 failures, 0 errors.
```

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

### Configuration

The [config.properties](https://github.com/dpapathanasiou/tweet-secret/blob/master/config.properties) file defines various settings used by the application for both encoding and decoding:

- *tweet-size* defines the maximum text size for an encoded tweet text. 

  The default is 140 characters which is the constraint imposed by Twitter, but this can be increased or decreased if desired. Note that changing it to anything but a positive, non-zero integer value will result in an error.

- *excess-marker* is the unobtrusive marker character within the encoded tweet text. 

  It is set by default as the <a href="http://www.fileformat.info/info/unicode/char/00b7/index.htm" target="_blank">Unicode Middle Dot (Latin-1 Supplement)</a> character.

- *dictionary-files* defines a whitespace-separated list of files or urls containing text word lists to use for the first-pass of plaintext encoding.

  It includes both the <tt>/usr/share/dict/words</tt> local file, which is the default for English on mac osx and most linux systems, and [http://www.cs.duke.edu/~ola/ap/linuxwords](http://www.cs.duke.edu/~ola/ap/linuxwords) an example of a text list found online.

  Words in messages to be encoded need to exist in at least one of the files or urls defined here, so if your message uses specialized language or slang, you'll need to have the appropriate dictionary file(s) defined here as well.

- *corpus-parse-fn* defines the name of the function in the [/src/tweet-secret/languages.clj](https://github.com/dpapathanasiou/tweet-secret/blob/master/src/tweet_secret/languages.clj) file to use for parsing the corpus text into lists of grammatically correct sentences.

- *tokenize-fn* defines the name of the function in the [/src/tweet-secret/languages.clj](https://github.com/dpapathanasiou/tweet-secret/blob/master/src/tweet_secret/languages.clj) file to use for splitting the input message into a list of distinct tokens that are expected to be found in one or more of the dictionary-files property values.

The default is English, but *any language* can be supported, as long as two functions are implemented in the [/src/tweet-secret/languages.clj](https://github.com/dpapathanasiou/tweet-secret/blob/master/src/tweet_secret/languages.clj) file.

1. A function to parse a string into a list of sentence strings, and 
2. A function to split a message string into word tokens, where each token be expected to be found in one or more of the dictionary-files property values (as explained above).

Function (1) corresponds to *corpus-parse-fn* and (2) corresponds to *tokenize-fn*.

### Exceptions and Warnings

- The *tweet-size* value in [config.properties](https://github.com/dpapathanasiou/tweet-secret/blob/master/config.properties) must be a positive, non-zero integer value.

- The "<i>-c</i>" or "<i>--corpus</i>" command line argument is required.

  This is the secret "book" known only by you and the people you want to be able to understand your message (see the Examples, below, for some ideas of what to use here).

- The corpus text needs to be large enough so that it can parse enough eligible tweets (sentences that are *tweet-size* characters long or less) so that the number of eligible tweets is greater than or equal to the total number of words defined by the *dictionary-files* property in [config.properties](https://github.com/dpapathanasiou/tweet-secret/blob/master/config.properties), because their relative sizes and positions is how the application maps words in the message to correspond to corpus tweets to broadcast.

  This is not something you need to calculate in advance, but the best way to avoid it is to use the largest texts you can access, and ideally more than just one at a time, for security (see the Examples, below, for what this would look like). 

  If you do wind up using a corpus text which is too small, the application will give you this error message:

```
Sorry, your corpus text is not large enough. Please use a larger text, or, include additional --corpus options and try again.

```

- The words in the message you wish to encode must exist in the contents of at least one of the *dictionary-files* defined in [config.properties](https://github.com/dpapathanasiou/tweet-secret/blob/master/config.properties).

  To avoid this problem, add files or urls to the *dictionary-files* value in [config.properties](https://github.com/dpapathanasiou/tweet-secret/blob/master/config.properties) which are sure to contain the words in your message, otherwise, the application will give you this warning:

```
	[WARNING]: "booyah" could not be processed
```

- If you try to decode a tweet which does not exist among the list of eligible tweets (sentences derived from parsing all the corpus texts), you will be greeted with a <tt>[WARNING]: "..." could not be processed</tt> warning.

Examples
--------

Suppose we want to encode the message "<b>Tonight we take Paris by storm</b>" as a series of innocuous-looking tweets. 

Let's use <a href="http://textfiles.com/etext/NONFICTION/mexico" target="_blank">The History Of The Conquest Of Mexico</a> (<a href="http://textfiles.com/etext/NONFICTION/mexico" target="_blank">http://textfiles.com/etext/NONFICTION/mexico</a>) by William Hickling Prescott on [textfiles.com](http://textfiles.com/) as the randomly selected corpus text.

The corpus text is known only by us and the followers we want to be able to read the message. The corpus text should be changed frequently, and ideally, never ever used more than once. It is also a good idea to use a list of several corpus texts in practice, since this lessens the chances that someone spying could guess the corpus and break the code (see below, after this first example, for what that looks like).

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

### Multiple Corpus Texts (recommended)

The encoding can also be done with multiple corpus texts, either all remote urls, or mixing urls and plain text files available on your computer's filesystem.

**Using multiple corpus texts instead of just a single corpus text is highly recommended**, since it reduces the likelihood that someone attempting to crack your secret message discovers the underlying pattern. It also help you avoid the <tt>corpus text is not large enough</tt> error.

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

Future TODO
------------

- Come up with a better strategy for handling message words which are not defined in the default *dictionary-files* texts
- Pack multiple short tweets together into a single broadcast tweet, space-permitting, so that it's not always a 1:1 correspondence between words in the message to tweets (not only harder to break, but also more efficient use of bandwidth)
- Create a graphical user interface in [Swing](http://en.wikipedia.org/wiki/Swing_%28Java%29), [Standard Widget Toolkit](http://en.wikipedia.org/wiki/Standard_Widget_Toolkit), or [Seesaw](https://github.com/daveray/seesaw) as an alternative to the command line interface
- Use the [Twitter API](https://dev.twitter.com/) to post tweets automatically, if an application has been defined, and the relevant [application OAuth settings](https://dev.twitter.com/docs/application-permission-model) (Consumer key, Consumer secret, etc.) have been defined in [config.properties](https://github.com/dpapathanasiou/tweet-secret/blob/master/config.properties)

