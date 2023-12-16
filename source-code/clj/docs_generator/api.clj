
(ns docs-generator.api
    (:require [docs-generator.core.engine :as core.engine]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Expected syntax
;
; @code
; (defn my-function
;   ; @warning
;   ; This is a very useful sample function! Use carefully!
;   ;
;   ; @description
;   ; Returns the given parameters in a map.
;   ;
;   ; @param (string) a
;   ; @param (map)(opt) b
;   ; {:c (string)(opt)}
;   ;
;   ; @usage
;   ; (my-function {...})
;   ;
;   ; @usage
;   ; (my-function "..." {...})
;   ;
;   ; @example
;   ; (my-function "A" {:c "C"})
;   ; =>
;   ; {:a "A" :b {:c "C"}}
;   ;
;   ; @example
;   ; (my-function "A" "B")
;   ; =>
;   ; nil
;   ;
;   ; @return (map)
;   ; {:a (string)
;   ;  :b (map)}
;   [a & [b]]
;   (and (string? a)
;        (map?    b)
;        {:a a :b b}))

; @tutorial How to generate documentation books?
;
; The ['docs-generator.api/create-documentation!'](#create-documentation!) function reads the files from the given 'code-dirs' folders,
; generates the documentation and prints the markdown files to the 'output-dir' folder.
;
; @important
; The 'create-documentation!' function erases the output directory before printing the documentation books!
;
; @usage
; (create-documentation! {:code-dirs  ["src/clj" "src/cljc" "src/cljs"]
;                         :lib-name   "My library"
;                         :output-dir "documentation"})
;
; You can set the author name and website printed to books.
;
; @usage
; (create-documentation! {:author     "Author"
;                         :code-dirs  ["src/clj" "src/cljc" "src/cljs"]
;                         :lib-name   "My library"
;                         :output-dir "documentation"
;                         :website    "httsp://github.com/author/my-repository"})
;
; By using the {:abs-path "..."} setting, you can specify where is your code placed within the project directory.
; It might be useful if you want to create documentation books for submodules or subprojects.
;
; @usage
; (create-documentation! {:abs-path   "submodules/my-repository"
;                         :code-dirs  ["src/clj" "src/cljc" "src/cljs"]
;                         :lib-name   "My library"
;                         :output-dir "documentation"})
;
; By using the {:print-options [...]} setting, you can specify the information you want to be printed into the books.
;
; @usage
; (create-documentation! {:code-dirs  ["src/clj" "src/cljc" "src/cljs"]
;                         :lib-name   "My library"
;                         :output-dir "documentation"
;                         :print-options [:code :description :examples :params :require :return :usages :warning]})

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (docs-generator.core.engine/*)
(def create-documentation! core.engine/create-documentation!)
(def debug                 core.engine/debug)
