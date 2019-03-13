% File: cannibal.pl
% Date: 1 November 2018
% Author: Matthew Morgan
% Tabs: 2
%
% pl -o can -c cannibal.pl
% ./can
% 
% A state is represented as [[LM,LC], [RM,RC], Boat]:
% * 

% mis(In,Out) and can(In,Out) return the number of missionaries and cannibals.
mis([M, _ | []], M).
can([_, C | []], C).

% banks(In,Out,Out,Out) pulls apart a state.
% state(In,In,In,Out) puts a state back together.
banks([L, R, B | []], L, R, B).
state(L,R,B, [L,R,B]).

% debug(S,St,NSt) :- write(S), write(': '), write(St), write(' -> '), write(NSt), nl.
debug(_,_,_).

% ------------------------------------------------------------------------------
% L bank --> R bank
% Move two missionaries
% Move two cannibals
% Move a missionary and cannibal
% Move a missionary
% Move a cannibal
follows(S,T) :-
  banks(S,L,R,B), B == l, mis(L,LM), LM > 1, 
  mis(R,RM), can(L,LC), can(R,RC),
  NLM is LM - 2, NRM is RM + 2,
  state([NLM,LC], [NRM,RC], r, T),
  debug(1,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == l, can(L,LC), LC > 1, 
  can(R,RC), mis(L,LM), mis(R,RM),
  NLC is LC - 2, NRC is RC + 2,
  state([LM,NLC], [RM,NRC], r, T),
  debug(2,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == l, mis(L,LM), can(L,LC), LM > 0, LC > 0,
  mis(R,RM), can(R,RC),
  NRM is RM + 1, NRC is RC + 1, NLM is LM - 1, NLC is LC - 1,
  state([NLM,NLC], [NRM,NRC], r, T),
  debug(0,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == l, mis(L,LM), LM > 0, 
  mis(R,RM), can(L,LC), can(R,RC),
  NLM is LM - 1, NRM is RM + 1,
  state([NLM,LC], [NRM,RC], r, T),
  debug(3,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == l, can(L,LC), LC > 0, 
  can(R,RC), mis(L,LM), mis(R,RM),
  NLC is LC - 1, NRC is RC + 1,
  state([LM,NLC], [RM,NRC], r, T),
  debug(4,S,T).

% R bank -> L bank
follows(S,T) :-
  banks(S,L,R,B), B == r, mis(R,RM), RM > 1, 
  mis(L,LM), can(L,LC), can(R,RC),
  NRM is RM - 2, NLM is LM + 2,
  state([NLM,LC], [NRM,RC], l, T),
  debug(6,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == r, can(R,RC), RC > 1, 
  can(L,LC), mis(L,LM), mis(R,RM),
  NRC is RC - 2, NLC is LC + 2,
  state([LM,NLC], [RM,NRC], l, T),
  debug(7,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == r, mis(R,RM), can(R,RC), RM > 0, RC > 0,
  mis(L,LM), can(L,LC),
  NLM is LM + 1, NLC is LC + 1, NRM is RM - 1, NRC is RC - 1,
  state([NLM,NLC], [NRM,NRC], l, T),
  debug(5,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == r, mis(R,RM), RM > 0, 
  mis(L,LM), can(L,LC), can(R,RC),
  NRM is RM - 1, NLM is LM + 1,
  state([NLM,LC], [NRM,RC], l, T),
  debug(8,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == r, can(R,RC), RC > 0, 
  can(L,LC), mis(L,LM), mis(R,RM),
  NRC is RC - 1, NLC is LC + 1,
  state([LM,NLC], [RM,NRC], l, T),
  debug(9,S,T).

% ------------------------------------------------------------------------------
admissible(S) :- banks(S,[LM,LC|[]],[RM,RC|[]],_),
  (LM > LC; LM == LC; LM == 0),
  (RM > RC; RM == RC; RM == 0).

newState([Start | R], [NS, Start | R]) :- %sleep(1),
  follows(Start, NS), admissible(NS), \+(member(NS, [Start | R])).

plan([Goal | R], Goal, [Goal | R]).
plan(PS, Goal, FS) :- newState(PS, LS), plan(LS, Goal, FS).

planShow([]).
planShow([H | T]) :-
  format('[[LM,LC], [RM,RC], B]: ~w~n', [H]),
  planShow(T).
  
run :-
  Emp = [[3,3], [0,0], l],
  Ful = [[0,0], [3,3], r],
  plan([Emp], Ful, Plan),
  reverse(Plan, RPlan),
  planShow(RPlan),
  nl.
