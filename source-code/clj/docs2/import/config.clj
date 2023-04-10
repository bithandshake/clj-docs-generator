
(ns docs2.import.config)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (regex pattern)
; Matches a constant if it's ...
; ... placed right after a newline character (drops out the matches from comments)
; ... starts with "(def "
; ... the name might contains LC or UC letters, and special chars: \- \_ \? \! \< \>
(def CONSTANT-DECLARATION-PATTERN #"\n\(def[ \t\n]{1,}[a-zA-Z\d\-\_\?\!\<\>]{1,}[ \t\n]{1,}")

; @ignore
;
; @constant (regex pattern)
(def FUNCTION-DECLARATION-PATTERN #"\n\(defn[\-]{0,}[ \t\n]{1,}[a-zA-Z\d\-\_\?\!\<\>]{1,}[ \t\n]{1,}")
