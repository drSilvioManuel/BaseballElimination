import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.FlowEdge;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;


public class BaseballElimination {

    private static final int GAME_INDEX = 4;
    private final int teamsNumber;
    private final ST<String, Integer> teamsToIndex;
    private final String[] teamsToNames;
    private final int[] wins;
    private final int[] losses;
    private final int[] remaining;
    private final int[][] games;
    private final int maxWin;
    private final ST<String, Bag<String>> gameGraph;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        throwExceptionIfNull(filename);

        In input = new In(filename);
        String line = input.readLine();
        teamsNumber = null != line ? Integer.parseInt(line.trim()) : 0;

        teamsToIndex = new ST<>();
        teamsToNames = new String[teamsNumber];
        wins = new int[teamsNumber];
        losses = new int[teamsNumber];
        remaining = new int[teamsNumber];
        games = new int[teamsNumber][teamsNumber];
        gameGraph = new ST<>();
        int m = -1;
        for (int i = 0; i < teamsNumber; i++) {
            setData(getTokens(input), i);
            if (m < wins[i]) m = wins[i];
        }
        maxWin = m;
    }

    // number of teamsToIndex
    public int numberOfTeams() {
        return teamsToIndex.size();
    }

    // all teamsToIndex
    public Iterable<String> teams() {
        return teamsToIndex.keys();
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

        return certificateOfElimination(team) != null;
    }

    // subset R of teamsToIndex that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        int x = getIndex(team);
        Bag<String> result;
        if (!gameGraph.contains(team)) {

            FlowNetwork flow = new FlowNetwork(teamsNumber * teamsNumber + 2);

            int s = teamsNumber;
            int t = teamsNumber + 1;
            result = new Bag<>();
            fillFlow(x, flow, s, t);

            FordFulkerson fordFulkerson = new FordFulkerson(flow, s, t);
            for (int i = 0; i < teamsNumber; i++) {
                if (fordFulkerson.inCut(i)) {
                    result.add(teamsToNames[i]);
                }
            }
            gameGraph.put(team, result);
        } else {
            result = gameGraph.get(team);
        }

        return result.isEmpty() ? null : result;
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
        int nextVertex = t + 1;

        for (int i = 0; i < teamsNumber; i++) {
            for (int j = 0; j < teamsNumber; j++) {

                if (i == x || j == x) continue;
                if (games[i][j] == 0) continue;

                int combinedNode = nextVertex++;

                FlowEdge edge = new FlowEdge(s, combinedNode, games[i][j]);
                flow.addEdge(edge);

                edge = new FlowEdge(combinedNode, i, Double.POSITIVE_INFINITY);
                flow.addEdge(edge);

                edge = new FlowEdge(combinedNode, j, Double.POSITIVE_INFINITY);
                flow.addEdge(edge);

                if (!set.contains(i)) {
                    int capacity = wrX-wins[i];
                    edge = new FlowEdge(i, t, (capacity < 0 ? 0 : capacity));
                    flow.addEdge(edge);
                    set.add(i);
                }

                if (!set.contains(j)) {
                    int capacity = wrX-wins[j];
                    edge = new FlowEdge(j, t, (capacity < 0 ? 0 : capacity));
                    flow.addEdge(edge);
                    set.add(j);
                }
            }
        }
    }

    private String[] getTokens(In input) {
        StringTokenizer st = new StringTokenizer(input.readLine(), " ");
        String[] tmpTokens = new String[teamsNumber + GAME_INDEX];

        int i = 0;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals("")) continue;
            tmpTokens[i++] = token;
        }
        return Arrays.copyOfRange(tmpTokens, 0, i);
    }

    private static void throwExceptionIfNull(Object... args) {
        for (Object arg : args)
            if (arg == null) throw new IllegalArgumentException();
    }

    private int getIndex(String team) {
        throwExceptionIfNull(team);
        Integer i = teamsToIndex.get(team);
        throwExceptionIfNull(i);
        return i;
    }

    private void setData(String[] tokens, int i) {

        teamsToIndex.put(tokens[0], i);
        teamsToNames[i] = tokens[0];
        wins[i] = Integer.parseInt(tokens[1]);
        losses[i] = Integer.parseInt(tokens[2]);
        remaining[i] = Integer.parseInt(tokens[3]);

        for (int j = GAME_INDEX; j < tokens.length; j++) {
            games[i][j - GAME_INDEX] = Integer.parseInt(tokens[j]);
        }
    }
}