" Vim syntax file for tao code

" Quit when a (custom) syntax file was already loaded
if exists("b:current_syntax")
  finish
endif

" keywords
syn keyword taoKeyword if elif else while for in def lambda return and or not var let type of match case
syn keyword taoKeyword assert break continue pass class

" types
syn keyword taoType Int Float String Bool True False

" comments and TODOs
syn keyword taoTodo contained TODO FIXME
syn match taoComment "#.*$" contains=taoTodo

" values
syn match taoValue '\<\d\+'
syn match taoValue '[-+]\d\+'

" floating point value regexes taken from c.vim syntax file
syn match taoValue '\<\d\+\.\d*\(e[-+]\=\d\+\)\=[fl]\='
syn match taoValue '\<\.\d\+\(e[-+]\=\d\+\)\=[fl]\=\>'
syn match taoValue '\<\d\+e[-+]\=\d\+[fl]\=\>'
syn region taoValue start='"' end='"' skip='\\"'

" hook it up
hi def link taoKeyword Keyword
hi def link taoType Type
hi def link taoValue Constant
hi def link taoComment Comment
hi def link taoTodo Todo
hi def link taoError Error

