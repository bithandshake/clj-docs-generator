
(ns docs2.import.utils
    (:require [docs2.import.config :as import.config]
              [regex.api          :as regex :refer [re-mismatch?]]
              [string.api         :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn constant-ignored?
  ; @param (string) file-content
  ; @param (string) match
  ;
  ; @usage
  ; (constant-ignored? "..." "...")
  ;
  ; @return (boolean)
  [file-content match]
  ; 1. Takes the part of the content before the match.
  ; 2. Takes the part between the match and the last occurence of the ignore mark.
  ; 3. Determines whether the found ignore mark belongs to the match or not.
  ;    If at least one line follows the ignore mark that isn't a comment line means
  ;    the mark doesn't belongs to the constant.
  (letfn [(f [observed-part] (and observed-part (or (re-mismatch? observed-part #"\n[ ]{0,}(?!;)")
                                                    (re-mismatch? observed-part #"\n"))))]
         (-> file-content (string/before-first-occurence match   {:return? false})
                          (string/after-last-occurence "@ignore" {:return? false})
                          (f))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn match->constant-name
  ; @param (string) match
  ;
  ; @example
  ; (match->constant-name "\n(def my-constant "My value")")
  ; =>
  ; "my-constant"
  ;
  ; @return (string)
  [match]
  (-> match (regex/after-first-occurence #"\(def[ ]{1,}" {:return? false})
            (string/trim)
            (string/before-first-occurence " " {:return? false})
            (string/trim)))

(defn match->function-name
  ; @param (string) match
  ;
  ; @example
  ; (match->function-name "\n(defn my-function [] ...)")
  ; =>
  ; "my-function"
  ;
  ; @return (string)
  [match]
  (-> match (regex/after-first-occurence #"\(defn[\- ]{1,}" {:return? false})
            (string/trim)
            (string/before-first-occurence " " {:return? false})
            (string/trim)))
