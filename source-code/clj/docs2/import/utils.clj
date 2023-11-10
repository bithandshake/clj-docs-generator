
(ns docs2.import.utils
    (:require [docs2.import.config :as import.config]
              [regex.api          :as regex :refer [re-mismatch?]]
              [string.api         :as string]
              [syntax-reader.api  :as syntax-reader]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn function-name->function-declaration-pattern
  ; @param (string) function-name
  ;
  ; @usage
  ; (function-name->function-declaration-pattern "my-function")
  ;
  ; @return (regex pattern)
  [function-name]
  (let [pattern (str "\\n\\(defn[\\-]{0,}[ \\t\\n]{1,}"function-name"[ \\t\\n]{1,}")]
       (-> pattern (string/replace-part "?" "\\?")
                   (string/replace-part "!" "\\!")
                   (re-pattern))))

(defn constant-name->constant-declaration-pattern
  ; @param (string) constant-name
  ;
  ; @usage
  ; (constant-name->constant-declaration-pattern "my-constant")
  ;
  ; @return (regex pattern)
  [constant-name]
  (let [pattern (str "\\n\\(def[ \\t\\n]{1,}"constant-name"[ \\t\\n]{1,}")]
       (-> pattern (string/replace-part "?" "\\?")
                   (string/replace-part "!" "\\!")
                   (re-pattern))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn function-position
  ; @param (string) file-content
  ; @param (string) function-name
  ;
  ; @example
  ; (function-position "..." "my-function")
  ; =>
  ; 42
  ;
  ; @return (integer)
  [file-content function-name]
  (->> function-name (function-name->function-declaration-pattern)
                     (regex/first-dex-of file-content)))

(defn constant-position
  ; @param (string) file-content
  ; @param (string) constant-name
  ;
  ; @example
  ; (constant-position "..." "my-constant")
  ; =>
  ; 42
  ;
  ; @return (integer)
  [file-content constant-name]
  (->> constant-name (constant-name->constant-declaration-pattern)
                     (regex/first-dex-of file-content)))

(defn function-header
  ; @param (string) file-content
  ; @param (string) function-name
  ;
  ; @usage
  ; (function-header "..." "...")
  ;
  ; @return (string)
  [file-content function-name]
  (as-> function-name % (function-name->function-declaration-pattern %)
                        (regex/after-first-occurence file-content %)
                        (regex/before-first-occurence % #"\n[ \t]{1,}[\[\(]{1,}")
                        (string/trim %)
                        (regex/replace-part % #"\n[ \t]{1,};" "\n;")))

(defn function-body
  ; @param (string) file-content
  ; @param (string) function-name
  ;
  ; @usage
  ; (function-body "..." "...")
  ;
  ; @return (string)
  [file-content function-name]
  (as-> function-name % (function-name->function-declaration-pattern %)
                        (regex/from-first-occurence file-content %)
                        (let [close-position (syntax-reader/paren-closing-position %)]
                             (string/part % 0 close-position))
                        (regex/from-first-occurence % #"\n[ \t]{1,}[\[\(]{1,}")))

(defn constant-header
  ; @param (string) file-content
  ; @param (string) constant-name
  ;
  ; @usage
  ; (constant-header "..." "...")
  ;
  ; @return (string)
  [file-content constant-name]
  ; These steps find the comment block (single- or multiline) right after the
  ; declaration of the constant.
  (as-> constant-name % (constant-name->constant-declaration-pattern %)
                        ; File content before the declaration of the constant
                        (regex/before-first-occurence file-content %)
                        ; Result after the first empty row
                        (regex/after-last-occurence % #"\n\n"           {:return? false})
                        ; Result from the first comment line
                        (regex/from-first-occurence % #"(?m)^[ ]*;"     {:return? false})
                        ; Result after the last non-comment line if any, because the result
                        ; is a comment block but not necessarily belongs to the constant.
                        ; Maybe there are non-comment rows between the found block and the constant.
                        (regex/after-last-occurence % #"(?m)^[ ]*(?!;)" {:return? true})))

(defn constant-value
  ; @param (string) file-content
  ; @param (string) constant-name
  ;
  ; @usage
  ; (constant-value "..." "...")
  ;
  ; @return (string)
  [file-content constant-name]
  ; The constant value could be a function that is multiline and contains
  ; multiple parenthesis!
  ; (def my-constant (fn [%]
  ;                      (...)))
  (as-> constant-name % (constant-name->constant-declaration-pattern %)
                        (regex/from-first-occurence file-content %)
                        (let [close-position (syntax-reader/paren-closing-position %)]
                             (string/part % 0 close-position))
                        (string/trim %)
                        (regex/after-last-occurence % #"[ \t]{1,}")))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn constant-ignored?
  ; @param (string) file-content
  ; @param (string) constant-name
  ;
  ; @usage
  ; (constant-ignored? "..." "...")
  ;
  ; @return (boolean)
  [file-content constant-name]
  (if-let [constant-header (constant-header file-content constant-name)]
          (regex/starts-with? constant-header #";[ ]{0,}@ignore")))

(defn function-ignored?
  ; @param (string) file-content
  ; @param (string) function-name
  ;
  ; @usage
  ; (function-ignored? "..." "...")
  ;
  ; @return (boolean)
  [file-content function-name]
  (if-let [function-header (function-header file-content function-name)]
          (regex/starts-with? function-header #";[ ]{0,}@ignore")))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn match->constant-name
  ; @param (string) match
  ;
  ; @example
  ; (match->constant-name "\n(def my-constant ")
  ; =>
  ; "my-constant"
  ;
  ; @return (string)
  [match]
  (-> match (regex/after-first-occurence #"\(def" {:return? false})
            (string/trim)))

(defn match->function-name
  ; @param (string) match
  ;
  ; @example
  ; (match->function-name "\n(defn my-function ")
  ; =>
  ; "my-function"
  ;
  ; @return (string)
  [match]
  (-> match (regex/after-first-occurence #"\(defn[\-]{0,}" {:return? false})
            (string/trim)))
