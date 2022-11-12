
(ns docs.process.engine
    (:require [candy.api            :refer [return]]
              [docs.process.helpers :as process.helpers]
              [docs.process.state   :as process.state]
              [docs.reader.state    :as reader.state]
              [io.api               :as io]
              [regex.api            :as regex]
              [mid-fruits.string    :as string]
              [syntax.api           :as syntax]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-return
  ; @param (string) header
  ;
  ; @example
  ;  (process-return "...")
  ;  =>
  ;  (?)
  ;
  ; @return (?)
  [header]
  (process.helpers/return header))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-first-usage
  ; @param (string) header
  ; @param (integer) cursor
  ;
  ; @example
  ;  (process-first-usage "... ; @usage (my-function ...) ..." 42)
  ;  =>
  ;  {:call "(my-function ...)"}
  ;
  ; @return (map)
  ;  {:call (string)}
  [header cursor]
  (-> header (string/part cursor)
             (process.helpers/first-usage)))

(defn process-usages
  ; @param (string) header
  ;
  ; @example
  ;  (process-usages "... ; @usage (my-function ...) ..." 42)
  ;  =>
  ;  [{:call "(my-function ...)"}]
  ;
  ; @return (maps in vector)
  ;  [{:call (string)}
  ;   {...}]
  [header]
  (letfn [(f [usages n]
             (if-let [cursor (string/nth-dex-of header "@usage" n)]
                     (let [usage (process-first-usage header cursor)]
                          (f (conj usages usage)
                             (inc n)))
                     (return usages)))]
         (f [] 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-first-example
  ; @param (string) header
  ; @param (integer) cursor
  ;
  ; @example
  ;  (process-first-example "... ; @example (my-function ...) => 123 ..." 42)
  ;  =>
  ;  {:call "(my-function ...)" :result 123}
  ;
  ; @return (map)
  ;  {:call (string)
  ;   :result (string)}
  [header cursor]
  (-> header (string/part cursor)
             (process.helpers/first-example)))

(defn process-examples
  ; @param (string) header
  ;
  ; @example
  ;  (process-examples "... ; @example (my-function ...) => 123 ..." 42)
  ;  =>
  ;  [{:call "(my-function ...)" :result 123}]
  ;
  ; @return (maps in vector)
  ;  [{:call (string)
  ;    :result (string)}
  ;   {...}]
  [header]
  (letfn [(f [examples n]
             (if-let [cursor (string/nth-dex-of header "@example" n)]
                     (let [example (process-first-example header cursor)]
                          (f (conj examples example)
                             (inc n)))
                     (return examples)))]
         (f [] 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-first-param
  ; @param (string) header
  ; @param (integer) cursor
  ;
  ; @example
  ;  (process-first-param "... ; @param (*)(opt) my-param ..." 42)
  ;  =>
  ;  {:name "my-param" :optional? true :types ["*"]}
  ;
  ; @return (map)
  ;  {:name (string)
  ;   :optional? (boolean)
  ;   :sample (string)
  ;   :types (strings in vector)}}
  [header cursor]
  (-> header (string/part cursor)
             (process.helpers/first-param)))

(defn process-params
  ; @param (string) header
  ;
  ; @example
  ;  (process-params "...")
  ;  =>
  ;  (?)
  ;
  ; @return (maps in vector)
  ;  [{:name (string)
  ;    :optional? (boolean)
  ;    :sample (string)
  ;    :types (strings in vector)}
  ;   {...}]
  [header]
  (letfn [(f [params n]
             (if-let [cursor (string/nth-dex-of header "@param" n)]
                     (let [param (process-first-param header cursor)]
                          (f (conj params param)
                             (inc n)))
                     (return params)))]
         (f [] 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-header
  ; @param (string) code
  ; @param (string) name
  ;
  ; @example
  ;  (process-header "..." "my-function")
  ;  =>
  ;  (?)
  ;
  ; @return (?)
  [code name]
  ; A letfn térben definiált függvények is rendelkezhetnek paraméter dokumentációval!
  (let [args-pos (regex/first-dex-of code #"[\n][ ]{1,}[\[]")
        header   (-> code (string/part 0 args-pos)
                          (string/after-first-occurence name {:return? false}))]
       {"params"   (process-params   header)
        "examples" (process-examples header)
        "usages"   (process-usages   header)
        "return"   (process-return   header)}))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-def_
  ; @param (string) file-content
  ; @param (string) name
  ;
  ; @example
  ;  (process-def "..." "my-function")
  ;  =>
  ;  (?)
  ;
  ; @return (?)
  [file-content name]
  (let [pattern (str "[(]def[ ]{1,}"name)]
       (if-let [pos (regex/first-dex-of file-content (re-pattern pattern))]
               pos)))

(defn process-defn
  ; @param (string) file-content
  ; @param (string) name
  ;
  ; @example
  ;  (process-defn "..." "my-function")
  ;  =>
  ;  (?)
  ;
  ; @return (?)
  [file-content name]
  (let [pattern (str "[(]defn[-]{0,}[ ]{1,}"name)]
       (if-let [start-pos (regex/first-dex-of file-content (re-pattern pattern))]
               (let [end-pos (-> file-content (string/part start-pos)
                                              (syntax/close-paren-position)
                                              (+ start-pos))]
                    (let [code (string/part file-content start-pos end-pos)]
                         {"header" (process-header code name)})))))

(defn process-code
  ; @param (string) file-content
  ; @param (string) name
  ;
  ; @example
  ;  (process-code "..." "my-function")
  ;  =>
  ;  (?)
  ;
  ; @return (?)
  [file-content name]
  (or (process-defn file-content name)
      (process-def_ file-content name)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-def
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (string) name
  ; @param (string) value
  ;
  ; @example
  ;  (process-def {:path "my-submodules/my-repository"} "clj" "my-directory")
  ;  =>
  ;  (?)
  ;
  ; @return (?)
  [options layer-name directory-name name value]
  (let [alias (or (string/before-first-occurence value "/" {:return? false})
                  (get-in @reader.state/LAYERS [layer-name directory-name "refers" value]))
        code-filepath (process.helpers/code-filepath options layer-name directory-name alias)]
       (if-let [file-content (io/read-file code-filepath)]
               (process-code file-content name)
               ; Ha a code-filepath útvonalon nem olvasható be forráskód fájl tartalma,
               ; akkor megpróbálja a cljc rétegben elérni a fájlt, mert előfordulhat,
               ; hogy a clj vagy cljs réteg api fájlja közvetlenül a cljc rétegből
               ; irányít át függvényeket vagy konstansokat.
               (let [code-filepath (process.helpers/code-filepath options "cljc" directory-name alias)
                     file-content  (io/read-file code-filepath)]
                    (process-code file-content name)))))

(defn process-defs
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @example
  ;  (process-defs {:path "my-submodules/my-repository"} "clj" "my-directory")
  ;  =>
  ;  {"my-function"   (?)
  ;   "your-function" (?)}
  ;
  ; @return (map)
  [options layer-name directory-name]
  (let [defs (get-in @reader.state/LAYERS [layer-name directory-name "defs"])]
       (letfn [(f [result name value]
                  (assoc result name (process-def options layer-name directory-name name value)))]
              (reduce-kv f {} defs))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-directory
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @example
  ;  (process-directory {:path "my-submodules/my-repository"} "clj" "my-directory")
  ;  =>
  ;  {"code" {}}
  ;
  ; @return (map)
  ;  {"code" (map)
  ;    {}}
  [options layer-name directory-name]
  {"code" (process-defs options layer-name directory-name)})

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-layer
  ; @param (map) options
  ; @param (string) layer-name
  ;
  ; @example
  ;  (process-layer {:path "my-submodules/my-repository"} "clj")
  ;  =>
  ;  {"my-directory" {}}
  ;
  ; @return (map)
  [options layer-name]
  (let [layer-data (get @reader.state/LAYERS layer-name)]
       (letfn [(f [layer-data directory-name directory-data]
                  (assoc layer-data directory-name (process-directory options layer-name directory-name)))]
              (reduce-kv f {} layer-data))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-layers!
  ; @param (map) options
  [options]
  (let [layers @reader.state/LAYERS]
       (letfn [(f [_ layer-name _]
                  (let [layer-data (process-layer options layer-name)]
                       (swap! process.state/LAYERS assoc layer-name layer-data)))]
              (reduce-kv f nil layers))))
