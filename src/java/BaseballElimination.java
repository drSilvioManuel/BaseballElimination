import edu.princeton.cs.algs4.*;
import sun.util.resources.cldr.ti.CurrencyNames_ti;

import java.util.*;


class BaseballElimination {

    private final ST<String, Integer> teams;
    private final String[] _teams;
    private final int[] wins;
    private final int[] losses;
    private final int[] remaining;
    private final int[][] games;
    private final int maxWin;
    private final ST<String, Bag<String>> gameGraph;
    private final int[] vertexCapacity;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        throwExceptionIfNull(filename);

        In input = new In(filename);

        int teamsNumber = Integer.valueOf(input.readLine().trim());

        teams = new ST<>();
        _teams = new String[teamsNumber];
        wins = new int[teamsNumber];
        losses = new int[teamsNumber];
        remaining = new int[teamsNumber];
        vertexCapacity = new int[teamsNumber];
        games = new int[teamsNumber][teamsNumber];
        gameGraph = new ST<>();
        int m = -1;
        for (int i = 0; i < teamsNumber; i++) {
            vertexCapacity[i] = teamsNumber+2;
            setData(getTokens(input), i);
            if (m < wins[i]) m = wins[i];
        }
        maxWin = m;
    }

    // number of teams
    public int numberOfTeams() {
        return teams.size();
    }

    // all teams
    public Iterable<String> teams() {
        return teams.keys();
    }

    // number of wins for given team
    public int wins(String team) {
        return wins[getIndex(team)];
    }

    // number of losses for given team
    public int losses(String team) {
        return losses[getIndex(team)];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        return remaining[getIndex(team)];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        return games[getIndex(team1)][getIndex(team2)];
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        int x = getIndex(team);
        if (wins[x] + remaining[x] < maxWin) return true;
        Bag<String> r = (Bag<String>) certificateOfElimination(team);

        return r.isEmpty();
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        int x = getIndex(team);
        Bag<String> result;
        if ( ! gameGraph.contains(team)) {

            FlowNetwork flow = new FlowNetwork(vertexCapacity[x]);

            int s = wins.length;
            int t = wins.length + 1;
            fillFlow(x, flow, s, t);
            FordFulkerson fordFulkerson = new FordFulkerson(flow, s, t);
            result = new Bag<>();
            for (int i=0; i<wins.length; i++) {
                if (fordFulkerson.inCut(i)) {
                    result.add(_teams[i]);
                }
            }
            gameGraph.put(team, result);
        } else {
            result = gameGraph.get(team);
        }

        return result;
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }

    private void fillFlow(int x, FlowNetwork flow, int s, int t) {

        int wrX = wins[x] + remaining[x];
        Set<Integer> set = new HashSet<>();

        for (int i=0; i<wins.length; i++) {
            for (int j=0; j<wins.length; j++) {

                if (i == x || j == x) continue;
                if (games[i][j] == 0) continue;

                int combinedNode = makeCombinedNode(i, j);

                FlowEdge edge = new FlowEdge(s, combinedNode, games[i][j]);
                flow.addEdge(edge);

                edge = new FlowEdge(combinedNode, i, Double.POSITIVE_INFINITY);
                flow.addEdge(edge);

                edge = new FlowEdge(combinedNode, j, Double.POSITIVE_INFINITY);
                flow.addEdge(edge);

                if ( ! set.contains(i)) {
                    edge = new FlowEdge(i, t, wrX-wins[i]);
                    flow.addEdge(edge);
                    set.add(i);
                }

                if ( ! set.contains(j)) {
                    edge = new FlowEdge(j, t, wrX-wins[j]);
                    flow.addEdge(edge);
                    set.add(j);
                }
            }
        }
    }

    private int makeCombinedNode(int a, int b) {
        int ab = a;
        ab = (ab << 16) | b;
        return ab;
    }

    private int[] splitCombinedNode(int ab) {
        int a = ab >> 16;
        int b = ((ab << 16) >> 16);
        return new int[] {a, b};
    }

    private String[] getTokens(In input) {
        return input.readLine().trim().split(" ");
    }

    private static void throwExceptionIfNull(Object... args) {
        for (Object arg : args)
            if (arg == null) throw new IllegalArgumentException();
    }

    private Integer getIndex(String team) {
        throwExceptionIfNull(team);
        Integer i = teams.get(team);
        throwExceptionIfNull(i);
        return i;
    }

    private void setData(String[] tokens, int i) {

        teams.put(tokens[0], i);
        _teams[i] = tokens[0];
        wins[i] = Integer.valueOf(tokens[1]);
        losses[i] = Integer.valueOf(tokens[2]);
        remaining[i] = Integer.valueOf(tokens[3]);

        for (int j=4; j < tokens.length; j++) {
            games[i][j-4] = Integer.valueOf(tokens[j]);
            if (games[i][i-4] != 0) vertexCapacity[i]++;
        }
    }
}