
(ns docs.sample
    (:require [docs.api :as docs]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; A submodule forráskódja a source-code mappában legyen!
;
; A submodule forráskódja clj, cljc és cljs mappákra legyen felosztva!
;
; A submodule forráskódja ezekben a clj, cljc és cljs mappákban legyen elhelyezve!
;
; Az api.clj, api.cljc és api.cljs ezekben a forráskód mappákban legyen elhelyezve!
;
; ⌄ my-project
;   ⌄ my-submodules
;     ⌄ my-repository
;       ⌄ source-code
;         ⌄ clj
;           ⌄ my_directory
;             api.clj
;         ⌄ cljc
;           ⌄ my_directory
;             api.cljc
;         ⌄ cljs
;           ⌄ my_directory
;             api.cljs
;
; Az api fájlokban a függvények és konstansok átirányításai (def ...) függvénnyel
; történjen!
;
; Az átirányításokban ne használj docstring szöveget!

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; A submodule forráskód fájljaiban a :require meghívások mindig az api fájlhoz
; viszonyítva történjenek!
;
; ⌄ my-project
;   ⌄ my-submodules
;     ⌄ my-repository
;       ⌄ source-code
;         ⌄ clj
;           ⌄ my_directory
;             api.clj
;             ⌄ my_subdirectory
;               my_file.clj
;               your-file.clj
;
; (ns my-submodule.my-handler.events
;     (:require [my-directory.my-subdirectory.my-file :as my-subdirectory.my-file]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; ⌄ my-project
;   ⌄ my-submodules
;     ⌄ my-repository
;       ⌄ source-code
;         ⌄ clj
;           ⌄ my_directory
;             ...
;         ⌄ cljc
;           ⌄ my_directory
;             ...
;         ⌄ cljs
;           ⌄ my_directory
;             ...
;
; (docs/create-documentation! {:path "my-submodules/my-repository"})
