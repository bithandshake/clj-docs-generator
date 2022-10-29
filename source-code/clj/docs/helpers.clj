
(ns docs.helpers
    (:require [mid-fruits.syntax :as syntax]
              [mid-fruits.string :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn first-alias
  ; @param (string) n
  ;
  ; @example
  ;  (first-alias "... [my-namespace :as my-alias] ...")
  ;  =>
  ;  ["my-namespace" "my-alias"]
  ;
  ; @return (strings in vector)
  [n]
  (let [namespace ""
        alias     ""]
       [namespace alias]))

(defn first-def
  ; @param (string) n
  ;
  ; @example
  ;  (first-def "... (def my-symbol my-value) (def ...")
  ;  =>
  ;  ["my-symbol" "my-value"]
  ;
  ; @return (strings in vector)
  [n]
  (let [symbol (-> n (string/after-first-occurence  " "   {:return? false})
                     (string/trim)
                     (string/before-first-occurence " "   {:return? false}))
        value  (-> n (string/after-first-occurence symbol {:return? false})
                     (string/trim)
                     (string/before-first-occurence ")"   {:return? false}))]
       [symbol value]))
