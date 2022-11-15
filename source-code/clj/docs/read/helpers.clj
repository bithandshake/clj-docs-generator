
(ns docs.read.helpers
    (:require [regex.api         :as regex]
              [mid-fruits.string :as string]
              [syntax.api        :as syntax]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn code-filepath
  ; @param (map) options
  ;  {:path (string)}
  ; @param (string) layer-name
  ; @param (string) alias
  ;
  ; @example
  ;  (code-filepath {:path "my-submodules/my-repository"} "clj" "my-directory" "my-subdirectory.my-file")
  ;  =>
  ;  "my-submodules/my-repository/source-code/clj/my_directory/my_subdirectory/my_file.clj"
  ;
  ; @return (string)
  [{:keys [path]} layer-name directory-name alias]
  (let [directory-name    (string/replace-part directory-name "-" "_")
        relative-filepath (-> alias (string/replace-part "." "/")
                                    (string/replace-part "-" "_"))]
       (str path "/source-code/"layer-name"/"directory-name"/"relative-filepath"."layer-name)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn first-param
  ; @param (string) n
  ;
  ; @example
  ;  (first-param "... ; @param (*) my-param ...")
  ;  =>
  ;  {"name" "my-param" "optional?" false "types" "*"}
  ;
  ; @example
  ;  (first-param "... ; @param (*)(opt) my-param ...")
  ;  =>
  ;  {"name" "my-param" "optional?" true "types" "*"}
  ;
  ; @example
  ;  (first-param "... ; @param (map) my-param {...} ...")
  ;  =>
  ;  {"name" "my-param" "optional?" false "sample" "{...}" "types" "map"}
  ;
  ; @return (map)
  ;  {"name" (string)
  ;   "optional?" (boolean)
  ;   "sample" (string)
  ;   "types" (string)}}
  [n]
  (let [param-name (-> n (string/after-first-occurence  "  ; @param" {:return? false})
                         (string/before-first-occurence "\n"         {:return? false})
                         (string/after-last-occurence   ")"          {:return? false})
                         (string/trim))
        optional?  (-> n (string/before-first-occurence param-name   {:return? false})
                         (string/contains-part?         "(opt)"))
        types      (-> n (string/after-first-occurence  "("          {:return? false})
                         (string/before-first-occurence ")"          {:return? false}))]
      {"name" param-name "optional?" optional? "types" types}))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn first-usage
  ; @param (string) n
  ;
  ; @example
  ;  (first-usage "...")
  ;  =>
  ;  {"call" "..."}
  ;
  ; @return (map)
  ;  {"call" (string)}
  [n]
  ; 1.
  ; 2.
  ; 3. A tartalommal rendelkező sorok elejéről eltávolítja a "  ; " részt
  ; 4. Törli a következő bekezdés előtti üres sorokat ("  ;")
  (let [call (-> n (string/after-first-occurence  "  ; @usage" {:return? false})
                   (string/before-first-occurence "  ; @"      {:return? true})
                   (string/remove-part            "  ; ")
                   (string/remove-part            "  ;\n"))]
       {"call" call}))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn first-example
  ; @param (string) n
  ;
  ; @example
  ;  (first-example "... ; @example (my-function ...) => 123 ...")
  ;  =>
  ;  {"call" "(my-function ...)" "result" 123}
  ;
  ; @return (map)
  ;  {"call" (string)
  ;   "result" (string)}
  [n]
  (let [call   (-> n (string/after-first-occurence  "  ; @example" {:return? false})
                     (string/before-first-occurence "  ; =>"       {:return? false})
                     (string/remove-part            "  ; "))
        result (-> n (string/after-first-occurence  "  ; =>"       {:return? false})
                     (string/before-first-occurence "  ; @"        {:return? true})
                     (string/remove-part            "  ; ")
                     (string/remove-part            "  ;\n"))]
       {"call" call "result" result}))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn return
  ; @param (string) n
  ;
  ; @example
  ;  (return "... ; @return (map) {...} ...")
  ;  =>
  ;  {"sample" "{...}" "types" "map"}
  ;
  ; @return (map)
  ;  {"sample" (string)
  ;   "types" (string)}
  [n]
  (let [types  (-> n (string/after-first-occurence  "  ; @return" {:return? false})
                     (string/after-first-occurence  "("           {:return? false})
                     (string/before-first-occurence ")"           {:return? false}))
        sample (-> n (string/after-first-occurence  "  ; @return" {:return? false})
                     (string/after-first-occurence  ")"           {:return? false}))]
       {"sample" sample "types" types}))
