% https://swi-prolog.discourse.group/t/example-of-prolog-trace-interception-4/4458
% https://swi-prolog.discourse.group/t/using-prolog-trace-interception-4/4376/5

% https://www.swi-prolog.org/pldoc/man?section=debugoverview
% https://swi-prolog.discourse.group/t/swi-prolog-in-the-browser-using-wasm/5650

:- [library(dicts)].
:- set_prolog_flag(gc, off).

:- dynamic trace_filter/1.
:- leash(-all).
:- visible(+all).

user:prolog_trace_interception(Port, Frame, _Choice, continue) :-
  prolog_frame_attribute(Frame, goal, Goal),
  prolog_frame_attribute(Frame, level, Level),
  % prolog_frame_attribute(Frame, parent_goal(_), Parent_goal),
  writeln(stack_frame{current_goal:Goal, level:Level, port:Port}).

female(pam).
female(liz).
female(pat).
female(ann).

male(tom).
male(bob).
male(jim).

parent(pam,bob).
parent(tom,bob).
parent(tom,liz).
parent(bob,ann).
parent(bob,pat).
parent(pat,jim).
parent(bob,peter).
parent(peter,jim).

mother(X,Y) :-
    parent(X,Y),
    female(X).

grandmother(X,Z) :-
    mother(X,Y),
    parent(Y,Z).

run :-
    asserta( trace_filter(prolog_trace_interception_examples:parent(_,_)), Ref_1 ),
    asserta( trace_filter(prolog_trace_interception_examples:female(_)), Ref_2 ),
    asserta( trace_filter(prolog_trace_interception_examples:mother(_,_)), Ref_3 ),
    trace,
    (
        mother(_,_),
        fail,
        !
    ;
        notrace,
        nodebug
    ),
    erase(Ref_1),
    erase(Ref_2),
    erase(Ref_3).