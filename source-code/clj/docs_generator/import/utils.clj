
(ns docs-generator.import.utils
    (:require [fruits.string.api :as string]
              [syntax-reader.api :as syntax-reader]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn first-refer
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @usage
  ; (first-refer "... [my-namespace :refer [my-refer another-refer]] ...")
  ; =>
  ; {"my-refer" "my-namespace"}
  ;
  ; @return (map)
  [n]
  (let [opening-bracket-position (syntax-reader/bracket-starting-position n)
        closing-bracket-position (syntax-reader/bracket-closing-position  n)
        bracket-content          (string/keep-range n (inc opening-bracket-position) closing-bracket-position)]
       (if (string/contains-part? bracket-content " :refer ")
           (let [namespace (-> bracket-content (string/before-first-occurence " "        {:return? false}))
                 refer     (-> bracket-content (string/after-first-occurence  " :refer " {:return? false})
                                               (string/after-first-occurence  "["        {:return? false})
                                               (string/before-first-occurence "]"        {:return? false})
                                               (string/split                  #" "))]
                (letfn [(f0 [refer name] (assoc refer name namespace))]
                       (reduce f0 {} refer))))))

(defn first-alias
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @usage
  ; (first-alias "... [my-namespace :as my-alias] ...")
  ; =>
  ; ["my-namespace" "my-alias"]
  ;
  ; @return (strings in vector)
  ; [(string) namespace
  ;  (string) alias]
  [n]
  (let [opening-bracket-position (syntax-reader/bracket-starting-position n)
        closing-bracket-position (syntax-reader/bracket-closing-position n)
        bracket-content          (string/keep-range n (inc opening-bracket-position) closing-bracket-position)]
       (if (string/contains-part? bracket-content " :as ")
           (let [namespace (-> bracket-content (string/before-first-occurence " "     {:return? false}))
                 alias     (-> bracket-content (string/after-first-occurence  " :as " {:return? false})
                                               (string/before-first-occurence " "     {:return? true}))]
                [namespace alias])
           (let [namespace (-> bracket-content (string/before-first-occurence " "     {:return? true}))
                 alias namespace]
                [namespace alias]))))

(defn first-def
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @usage
  ; (first-def "... (def my-name my-value) (def ...")
  ; =>
  ; ["my-name" "my-value"]
  ;
  ; @return (strings in vector)
  ; [(string) name
  ;  (string) value]
  [n]
  (let [name  (-> n (string/after-first-occurence  " " {:return? false})
                    (string/trim)
                    (string/before-first-occurence " " {:return? false}))
        value (-> n (string/after-first-occurence name {:return? false})
                    (string/trim)
                    (string/before-first-occurence ")" {:return? false}))]
       [name value]))
