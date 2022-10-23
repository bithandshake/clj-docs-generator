
;; -- Namespace ---------------------------------------------------------------
;; ----------------------------------------------------------------------------

(ns docs.sample
    (:require [docs.api :as docs]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; A submodule forráskódja a source-code mappában legyen, clj, cljc és cljs
; mappákra felosztva!
;
; A submodule forráskódja ezekben a clj, cljc és cljs mappákban legyen elhelyezve!
;
; Az api.clj, api.cljc és api.cljs ezekben a forráskód mappákban legyen elhelyezve!

; ⌄ my-project
;   ⌄ submodules
;     ⌄ my-repository
;       ⌄ source-code
;         ⌄ clj
;           ⌄ my-submodule
;             api.clj
;         ⌄ cljc
;           ⌄ my-submodule
;             api.cljc
;         ⌄ cljs
;           ⌄ my-submodule
;             api.cljs

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; A submodule forráskód fájljaiban a :require meghívások mindig az api fájlhoz
; viszonyítva történjenek!

; ⌄ my-project
;   ⌄ submodules
;     ⌄ my-repository
;       ⌄ source-code
;         ⌄ clj
;           ⌄ my-submodule
;             api.clj
;             ⌄ my-handler
;               events.clj
;               subs.clj

; (ns my-submodule.my-handler.events
;     (:require [my-submodule.my-handler.subs :as my-handler.subs]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; ⌄ my-project
;   ⌄ submodules
;     ⌄ my-repository
;       ⌄ source-code
;         ⌄ clj
;           ⌄ my-submodule
;             ...
;         ⌄ cljc
;           ⌄ my-submodule
;             ...
;         ⌄ cljs
;           ⌄ my-submodule
;             ...

;(docs/create-documentation! {:path "submodules/my-submodule-api"})
