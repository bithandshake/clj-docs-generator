
(ns docs.print.engine
    (:require [candy.api          :refer [return]]
              [docs.print.state   :as print.state]
              [docs.process.state :as process.state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-def
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (string) name
  ; @param (string) value
  ;
  ; @example
  ;  (print-def {:path "my-submodules/my-repository"} "clj" "my-directory")
  ;  =>
  ;  (?)
  ;
  ; @return (?)
  [options layer-name directory-name name value])
  ;(let [alias (or (string/before-first-occurence value "/" {:return? false})
  ;                (get-in @reader.state/LAYERS [layer-name directory-name "refers" value])
  ;      code-filepath (process.helpers/code-filepath options layer-name directory-name alias)
  ;;     (if-let [file-content (io/read-file code-filepath)]
    ;           (process-code file-content name)
               ; Ha a code-filepath útvonalon nem olvasható be forráskód fájl tartalma,
               ; akkor megpróbálja a cljc rétegben elérni a fájlt, mert előfordulhat,
               ; hogy a clj vagy cljs réteg api fájlja közvetlenül a cljc rétegből
               ; irányít át függvényeket vagy konstansokat.
    ;           (let [code-filepath (process.helpers/code-filepath options "cljc" directory-name alias)
    ;                 file-content  (io/read-file code-filepath)
    ;                (process-code file-content name)])

(defn print-defs
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @example
  ;  (print-defs {:path "my-submodules/my-repository"} "clj" "my-directory")
  ;  =>
  ;  {}
  ;
  ; @return (map)
  [options layer-name directory-name]
  (let [defs (get-in @process.state/LAYERS [layer-name directory-name "defs"])]
       (letfn [(f [result name value]
                  (assoc result name (print-def options layer-name directory-name name value)))]
              (reduce-kv f {} defs))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-directory
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @example
  ;  (print-directory {:path "my-submodules/my-repository"} "clj" "my-directory")
  ;  =>
  ;  {}
  ;
  ; @return (map)
  ;  {}
  [options layer-name directory-name]
  {"?" (print-defs options layer-name directory-name)})

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-layer
  ; @param (map) options
  ; @param (string) layer-name
  ;
  ; @example
  ;  (print-layer {:path "my-submodules/my-repository"} "clj")
  ;  =>
  ;  {"my-directory" {}}
  ;
  ; @return (map)
  [options layer-name]
  (let [layer-data (get @process.state/LAYERS layer-name)]
       (letfn [(f [layer-data directory-name directory-data]
                  (assoc layer-data directory-name (print-directory options layer-name directory-name)))]
              (reduce-kv f {} layer-data))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-layers!
  ; @param (map) options
  [options]
  (let [layers @process.state/LAYERS]
       (letfn [(f [_ layer-name _]
                  (let [layer-data (print-layer options layer-name)]
                       (swap! print.state/LAYERS assoc layer-name layer-data)))]
              (reduce-kv f nil layers))))
