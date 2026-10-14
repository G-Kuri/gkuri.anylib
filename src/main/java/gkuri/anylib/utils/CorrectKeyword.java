package gkuri.anylib.utils;

public class CorrectKeyword {

	public static String correctKeyword(String[] words, String input) {
        String best = input;
        int bestScore = Integer.MAX_VALUE;
 
        for (String kw : words) {
            int dist = levenshtein(input, kw);
            if (dist < bestScore) {
                bestScore = dist;
                best = kw;
            }
        }
 
        // 距離が離れすぎてたら訂正しない
        if (bestScore > 2) {
            return input;
        }
        return best;
    }
 
 
    private static int levenshtein(String a, String b) {
    	if(a.equals(b)) return 0;
    	
        int[][] dp = new int[a.length() + 1][b.length() + 1];
 
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
 
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }
 
}
