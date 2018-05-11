;;  Copyright (c) Andrew Stoeckley / Balcony Studio 2017 - 2018. All rights reserved.

;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the license file at the root directory of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

(ns assistant.log)

;; When *assert* is off, log and logx forms and their args compile away with no runtime overhead.

(defn log*
  "Cross-environment logging. JS requires console.log for Chrome Dev Tools assistance. On the Clojure side, see this tip for better printing in multithreaded scenarios:

http://yellerapp.com/posts/2014-12-11-14-race-condition-in-clojure-println.html

Explicitly joined newline character ensures printing from different threads doesn't upset the newlines. Calling flush explicitly assures a flush, while a newline character alone does not."
  [& logs]
  #?(:clj (do (print (str (apply str (interpose " " logs)) "\n")) (flush))
     :cljs (apply js/console.log logs)))

(defmacro log
  "Logs only when *assert* is true. In CLJS, uses JS console for Dev Tools support."
  [& logs]
  (when *assert*
    `(log* ~@logs)))

(defmacro logx
  "Log expressions when *assert* is true, each on a line, with the quoted expression followed by its evaluation."
  [& vs]
  (when *assert*
    `(do
       ~@(for [v vs]
           `(log* '~v ~v)))))
