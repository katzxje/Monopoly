import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the creation and shuffling of Chance and Community Chest card decks.
 */
public class CardDeckManager {

    /**
     * Creates and returns a new, shuffled Chance card deck.
     * 
     * @return A list of Card objects representing the Chance deck.
     */
    public static List<Card> createChanceDeck() {
        List<Card> chanceCards = new ArrayList<>();
        chanceCards.add(new Card("Advance to Go", CardType.MOVEMENT, 0));
        chanceCards.add(new Card("Advance to Illinois Avenue", CardType.MOVEMENT, 24));
        chanceCards.add(new Card("Advance to St. Charles Place", CardType.MOVEMENT, 11));
        chanceCards.add(new Card("Advance to nearest Railroad", CardType.NEAREST_RAILROAD));
        chanceCards.add(new Card("Advance to nearest Utility", CardType.NEAREST_UTILITY));
        chanceCards.add(new Card("Bank pays you dividend of $50", CardType.COLLECT_MONEY, 50));
        chanceCards.add(new Card("Get Out of Jail Free", CardType.GET_OUT_OF_JAIL_FREE));
        chanceCards.add(new Card("Go back 3 spaces", CardType.MOVE_BACKWARD, 3));
        chanceCards.add(new Card("Go to Jail", CardType.GO_TO_JAIL));
        chanceCards.add(new Card("Make general repairs: $25 per house, $100 per hotel", CardType.REPAIRS, 25, 100));
        chanceCards.add(new Card("Pay poor tax of $15", CardType.PAY_MONEY, 15));
        chanceCards.add(new Card("Take a trip to Reading Railroad", CardType.MOVEMENT, 5));
        chanceCards.add(new Card("Take a walk on the Boardwalk", CardType.MOVEMENT, 39));
        chanceCards.add(new Card("You have been elected Chairman of the Board: Pay $50 to each player", CardType.PAY_EACH_PLAYER, 50));
        chanceCards.add(new Card("Your building loan matures: Collect $150", CardType.COLLECT_MONEY, 150));
        
        Collections.shuffle(chanceCards);
        return chanceCards;
    }

    /**
     * Creates and returns a new, shuffled Community Chest card deck.
     * 
     * @return A list of Card objects representing the Community Chest deck.
     */
    public static List<Card> createCommunityChestDeck() {
        List<Card> communityChestCards = new ArrayList<>();
        communityChestCards.add(new Card("Advance to Go", CardType.MOVEMENT, 0));
        communityChestCards.add(new Card("Bank error in your favor: Collect $200", CardType.COLLECT_MONEY, 200));
        communityChestCards.add(new Card("Doctor's fee: Pay $50", CardType.PAY_MONEY, 50));
        communityChestCards.add(new Card("From sale of stock you get $50", CardType.COLLECT_MONEY, 50));
        communityChestCards.add(new Card("Get Out of Jail Free", CardType.GET_OUT_OF_JAIL_FREE));
        communityChestCards.add(new Card("Go to Jail", CardType.GO_TO_JAIL));
        communityChestCards.add(new Card("Holiday fund matures: Receive $100", CardType.COLLECT_MONEY, 100));
        communityChestCards.add(new Card("Income tax refund: Collect $20", CardType.COLLECT_MONEY, 20));
        communityChestCards.add(new Card("It's your birthday: Collect $10 from each player", CardType.COLLECT_FROM_EACH_PLAYER, 10));
        communityChestCards.add(new Card("Life insurance matures: Collect $100", CardType.COLLECT_MONEY, 100));
        communityChestCards.add(new Card("Pay hospital fees of $100", CardType.PAY_MONEY, 100));
        communityChestCards.add(new Card("Pay school fees of $50", CardType.PAY_MONEY, 50));
        communityChestCards.add(new Card("Receive $25 consultancy fee", CardType.COLLECT_MONEY, 25));
        communityChestCards.add(new Card("You are assessed for street repairs: $40 per house, $115 per hotel", CardType.REPAIRS, 40, 115));
        communityChestCards.add(new Card("You have won second prize in a beauty contest: Collect $10", CardType.COLLECT_MONEY, 10));
        communityChestCards.add(new Card("You inherit $100", CardType.COLLECT_MONEY, 100));
        
        Collections.shuffle(communityChestCards);
        return communityChestCards;
    }
} 