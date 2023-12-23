
(ns docs-generator.read.utils
    (:require [fruits.regex.api  :as regex]
              [fruits.string.api :as string]
              [syntax-reader.api :as syntax-reader]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn function-content
  ; @ignore
  ;
  ; @param (string) file-content
  ; @param (string) name
  ;
  ; @return (string)
  [file-content name]
  ; open-pattern: "(defn my-function"
  ;
  ; BUG#7710
  ; Előfordulhat, hogy az end-pos értéke nil!
  ; Pl.: Ha a függvényben lévő valamelyik comment nem egyenlő számú nyitó és záró
  ;      zárójelet tartalmaz, akkor ...
  ;
  ; Ha a függvény neve kérdőjelre végződik, akkor a regex megsértődik a "function-name?\n"
  ; kifejezésre, mert a kérdőjel különleges karakter, ezért azt külön kell kezelni!
  (let [open-pattern (-> (str "[(]defn[-]*[ ]+"name"\n")
                         (string/replace-part "?" "[?]"))]
       (if-let [start-pos (regex/first-dex-of file-content (re-pattern open-pattern))]
               (if-let [end-pos (-> file-content (string/keep-range start-pos)
                                                 (syntax-reader/paren-closing-position))]
                       (let [end-pos (+ end-pos start-pos 1)]
                            (string/keep-range file-content start-pos end-pos))
                       ; Már megtalálta a függvény elejét, tehát létezik de nem találja a végét
                       ; szóval valsz. van valami syntax hiba a kommentben.
                       (throw (Exception. (str "Unable to find end of function content: " name)))))))

(defn function-header
  ; @ignore
  ;
  ; @param (string) file-content
  ; @param (string) name
  ;
  ; @return (string)
  [file-content name]
  ; A letfn térben definiált függvények is rendelkezhetnek paraméter dokumentációval,
  ; ezért szükséges csak a függvény fejlécében olvasni!
  (if-let [function-content (function-content file-content name)]
          (-> function-content (string/from-first-occurence   "\n  ;"  {:return? false})
                               (string/before-first-occurence "\n  ([" {:return? true})
                               (string/before-first-occurence "\n  ["  {:return? true}))))

(defn function-code
  ; @ignore
  ;
  ; @param (string) file-content
  ; @param (string) name
  ;
  ; @return (string)
  [file-content name]
  (let [comment-pattern "[ ]*;"]
       (if-let [function-content (function-content file-content name)]
               (letfn [(f0 [function-content lap]
                           (if (= lap 512)
                               (do ; Ha az f függvény végtelen ciklusba kerül, akkor
                                   ; valószínüleg valamelyik kommentben egyenlőtlenül
                                   ; vannak elhelyezve a zárójelek vagy esetleg egy string
                                   ; tartalmaz ";", amit a függvény kommentnek érzékel!
                                   (println "Ooops! It looks like there is a syntax error in the function \"" name "\"")
                                   (-> function-content))

                               (if-let [start-pos (regex/first-dex-of function-content (re-pattern comment-pattern))]
                                       (let [comment (-> function-content (string/keep-range start-pos)
                                                                          (string/before-first-occurence "\n" {:return? true}))]
                                            (f0 (string/remove-first-occurence function-content (str comment "\n"))
                                                (inc lap)))
                                       (-> function-content))))]
                      (f0 function-content 0)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn function-first-param
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @usage
  ; (function-first-param "... ; @param (*) my-param ...")
  ; =>
  ; {"name" "my-param" "optional?" false "types" "*"}
  ;
  ; @usage
  ; (function-first-param "... ; @param (*)(opt) my-param ...")
  ; =>
  ; {"name" "my-param" "optional?" true "types" "*"}
  ;
  ; @usage
  ; (function-first-param "... ; @param (map) my-param {...} ...")
  ; =>
  ; {"name" "my-param" "optional?" false "sample" "{...}" "types" "map"}
  ;
  ; @return (map)
  ; {"name" (string)
  ;  "optional?" (boolean)
  ;  "sample" (string)
  ;  "types" (string)}}
  [n]
  (let [param-name (-> n (string/after-first-occurence  "  ; @param" {:return? false})
                         (string/before-first-occurence "\n"         {:return? true})
                         (string/after-last-occurence   ")"          {:return? false})
                         (string/trim))
        optional?  (-> n (string/before-first-occurence param-name   {:return? false})
                         (string/contains-part?         "(opt)"))
        types      (-> n (string/after-first-occurence  "("          {:return? false})
                         (string/before-first-occurence ")"          {:return? false}))
        sample     (-> n (string/after-first-occurence  "  ; @param" {:return? false})
                         (string/before-first-occurence "  ; @"      {:return? true})
                         (string/after-first-occurence  "\n"         {:return? false})
                         (string/remove-part            "  ; ")
                         (string/remove-part            "  ;\n")
                         (string/not-ends-with!         "\n")
                         (string/to-nil {:if-empty? true}))]
      {"name" param-name "optional?" optional? "sample" sample "types" types}))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn function-first-usage
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @usage
  ; (function-first-usage "...")
  ; =>
  ; {"call" "..."}
  ;
  ; @return (map)
  ; {"call" (string)}
  [n]
  ; 1.
  ; 2.
  ; 3. A tartalommal rendelkező sorok elejéről eltávolítja a "  ; " részt
  ; 4. Törli a következő bekezdés előtti üres sorokat ("  ;")
  ; 5. Ha már nem következik utána több bekezdés, akkor lemaradna a végéről a sortörés,
  ;   ezért szükésges biztosítani, hogy sortörésre végződjön!
  (let [call (-> n (string/after-first-occurence  "  ; @usage" {:return? false})
                   (string/before-first-occurence "  ; @"      {:return? true})
                   (string/remove-part            "  ; ")
                   (string/remove-part            "  ;\n")
                   (string/ends-with!             "\n"))]
       {"call" call}))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn function-first-example
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @usage
  ; (function-first-example "... ; @example (my-function ...) => 123 ...")
  ; =>
  ; {"call" "(my-function ...)" "result" 123}
  ;
  ; @return (map)
  ; {"call" (string)
  ;  "result" (string)}
  [n]
  (let [call   (-> n (string/after-first-occurence  "  ; @example" {:return? false})
                     (string/before-first-occurence "  ; =>"       {:return? false})
                     (string/remove-part            "  ; "))
        result (-> n (string/after-first-occurence  "  ; =>"       {:return? false})
                     (string/before-first-occurence "  ; @"        {:return? true})
                     (string/remove-part            "  ; ")
                     (string/remove-part            "  ;\n")
                     (string/ends-with!             "\n"))]
       {"call" call "result" result}))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn function-warning
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @usage
  ; (function-warning "; @warning ...")
  ; =>
  ; "..."
  ;
  ; @return (string)
  [n]
  (-> n (string/after-first-occurence  "  ; @warning" {:return? false})
        (string/before-first-occurence "  ; @"        {:return? true})
        (string/remove-part            "  ; ")
        (string/remove-part            "  ;\n")
        (string/ends-with!             "\n")))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn function-description
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @usage
  ; (function-description "; @description ...")
  ; =>
  ; "..."
  ;
  ; @return (string)
  [n]
  (-> n (string/after-first-occurence  "  ; @description" {:return? false})
        (string/before-first-occurence "  ; @"            {:return? true})
        (string/remove-part            "  ; ")
        (string/remove-part            "  ;\n")
        (string/ends-with!             "\n")))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn function-return
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @usage
  ; (function-return "... ; @return (map) {...} ...")
  ; =>
  ; {"sample" "{...}" "types" "map"}
  ;
  ; @return (map)
  ; {"sample" (string)
  ;  "types" (string)}
  [n]
  (let [types  (-> n (string/after-first-occurence  "  ; @return" {:return? false})
                     (string/after-first-occurence  "("           {:return? false})
                     (string/before-first-occurence ")"           {:return? false}))
        sample (-> n (string/after-first-occurence  "  ; @return" {:return? false})
                     (string/after-first-occurence  ")"           {:return? false})
                     (string/remove-part            "  ; ")
                     (string/remove-part            "  ;\n"))]
       {"sample" sample "types" types}))
