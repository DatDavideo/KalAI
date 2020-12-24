import info.kwarc.teaching.AI.Kalah.Interfaces.Terminal$;
import info.kwarc.teaching.AI.Kalah.Game;
import info.kwarc.teaching.AI.Kalah.Interfaces.Fancy.FancyInterface;

public class Main {
	public static void main(String[] args) {
		FancyInterface i = new FancyInterface(36);
		Terminal$ t = Terminal$.MODULE$;
		KalAI p1 = new KalAI();
		KalAI p2 = new KalAI();
		
		
		Game g1 = new Game(p1, p2, i, 6, 6);
		g1.play();
		

		/*int[] houses1 = {4, 4, 4, 4, 4, 4};
		int[] houses2 = {4, 4, 4, 4, 4, 4};
		
		KalahNode game = new KalahNode(true, 0, 0, houses1, houses2);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		while(true) {
			if(game.getMoving()) {
				System.out.print("Player 1 enter move: ");
			} else {
				System.out.print("Player 2 enter move: ");
			}
			String s = br.readLine();
			int m = Integer.parseInt(s);
			game = game.move(m - 1);
		}*/
	}
}
