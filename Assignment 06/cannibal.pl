% File: cannibal.pl
% Date: 1 November 2018
% Author: Matthew Morgan
% Tabs: 2
%
% A state is represented as the following:
% [ L R B ], where L and R are the left and right banks, and B is the bank of the boat
% + L and R are jailbird notation lists for missionaries/cannibals; i.e. [m, m, c]
% + B is l for the boat being on the left bank, or r for the boat being on the right
%
% pl -o can -c cannibal.pl
% ./can

% head(In,Out) gets the head of the list
head([H | _], H).

% tail(In,Out) gets the tail of the list
tail([], []).
tail([_ | T], T).

% -----------------------------------------------------------------------------
% mis(In,Out) counts the number of missionaries in the list
mis([], 0).
mis([m | T], Mis) :- mis(T,Tail), Mis is 1 + Tail.
mis([c | T], Mis) :- mis(T,Tail), Mis is Tail.

% can(In,Out) counts the number of cannibals in the list
can([], 0).
can([c | T], Can) :- can(T,Tail), Can is 1 + Tail.
can([m | T], Can) :- can(T,Tail), Can is Tail.

% lBank(In,Out) gets the left bank of the state S
% rBank(In,Out) gets the right bank of the state S
% bBank(In,Out) gets the bank of the boat
% banks(In,Out,Out,Out) gets the banks and boat bank of state s
% state(In,In,In,Out) builds the new state with the given banks/boat/bbank
lBank(S,L) :- head(S,L).
rBank(S,L) :- tail(S,T), head(T,L).
bBank(S,B) :- tail(S,T), tail(T,TT), head(TT,B).
banks(S,L,R,B) :- lBank(S,L), rBank(S,R), bBank(S,B).
state(L,R,B,S) :- append([L], [R], M), append(M, [B], S).

% r1Mis(In,Out) and r1Can(In,Out) remove a missionary and cannibal from the given bank
rMis(L,LT) :- tail(L,LT).
rCan(L,LT) :- reverse(L,RL), tail(RL,TRL), reverse(TRL,LT).

%debug(S,St,NSt) :- write(S), write(': '), write(St), write(' -> '), write(NSt), nl.
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
  rMis(L,LL), rMis(LL,LLL), append([m, m], R, RR),
  state(LLL,RR,r,T),
  debug(1,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == l, can(L,LC), LC > 1,
  rCan(L,LL), rCan(LL,LLL), append(R, [c, c], RR),
  state(LLL,RR,r,T),
  debug(2,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == l, mis(L,LM), can(L,LC), LM > 0, LC > 0,
  rMis(L,LL), rCan(LL,LLL), append([m], R, RR), append(RR, [c], RRR),
  state(LLL,RRR,r,T),
  debug(0,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == l, mis(L,LM), LM > 0,
  rMis(L,LL), append([m], R, RR),
  state(LL,RR,r,T),
  debug(3,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == l, can(L,LC), LC > 0,
  rCan(L,LL), append(R, [c], RR),
  state(LL,RR,r,T),
  debug(4,S,T).

% R bank -> L bank
follows(S,T) :-
  banks(S,L,R,B), B == r, mis(R,RM), RM > 1,
  rMis(R,RR), rMis(RR,RRR), append([m, m], L, LL),
  state(LL,RRR,l,T),
  debug(6,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == r, can(R,RC), RC > 1,
  rCan(R,RR), rCan(RR,RRR), append(L, [c, c], LL),
  state(LL,RRR,l,T),
  debug(7,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == r, mis(R,RM), can(R,RC), RM > 0, RC > 0,
  rMis(R,RR), rCan(RR,RRR), append([m], L, LL), append(LL, [c], LLL),
  state(LLL,RRR,l,T),
  debug(5,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == r, mis(R,RM), RM > 0,
  rMis(R,RR), append([m], L, LL),
  state(LL,RR,l,T),
  debug(8,S,T).

follows(S,T) :-
  banks(S,L,R,B), B == r, can(R,RC), RC > 0,
  rCan(R,RR), append(L, [c], LL),
  state(LL,RR,l,T),
  debug(9,S,T).

% ------------------------------------------------------------------------------
safe(Bank) :- mis(Bank,BM), can(Bank,BC), (BM > BC; BM == BC; BM == 0).

admissible(S) :- banks(S,L,R,_), safe(L), safe(R).

newState([Start | R], [NS, Start | R]) :- %sleep(1),
  follows(Start, NS), admissible(NS), \+(member(NS, [Start | R])).

plan([Goal | R], Goal, [Goal | R]).
plan(PS, Goal, FS) :- newState(PS, LS), plan(LS, Goal, FS).

planShow([]).
planShow([H | T]) :- banks(H,L,R,B),
  format('LR: ~14w, ~14w, Boat: ~w~n', [L, R, B]),
  planShow(T).
  
run :-
  Emp = [[m,m,m,c,c,c], [], l],
  Ful = [[], [m,m,m,c,c,c], r],
  plan([Emp], Ful, Plan),
  reverse(Plan, RPlan),
  planShow(RPlan),
  nl.
