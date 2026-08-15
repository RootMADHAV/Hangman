package com.LetterQuest.data.repository

import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.Word
import com.LetterQuest.domain.model.WordCategory

/**
 * The game's word catalog, grouped into player-selectable categories.
 *
 * Words are held in memory rather than in Room: the set is fixed at build time and
 * read-only at runtime, so a database would add migration cost with no benefit.
 *
 * Difficulty tracks word length and obscurity — EASY words are longer and common,
 * HARD words are short or specialised. Every category carries all three difficulties
 * so that a category choice never restricts the difficulty choice.
 */
internal object WordCatalog {

    val categories = listOf(
        WordCategory("animals", "Animals", "🐾"),
        WordCategory("movies", "Movies", "🎬"),
        WordCategory("tv_series", "TV Series", "📺"),
        WordCategory("books", "Books", "📚"),
        WordCategory("countries", "Countries", "🌍"),
        WordCategory("cities", "Cities", "🏙️"),
        WordCategory("sports", "Sports", "⚽"),
        WordCategory("foods", "Foods", "🍕"),
        WordCategory("music", "Music", "🎵"),
        WordCategory("landmarks", "Landmarks", "🗼"),
        WordCategory("nature", "Nature", "🌿"),
        WordCategory("science", "Science", "🔬"),
        WordCategory("technology", "Technology", "💻"),
        WordCategory("professions", "Professions", "👷"),
        WordCategory("space", "Space", "🚀"),
        WordCategory("mythology", "Mythology", "⚡"),
        WordCategory("geography", "Geography", "🗺️"),
        WordCategory("famous_quotes", "Famous Quotes", "💭")
    )

    /**
     * Fallback display icon for a category id that is not listed in [categories]
     * (e.g. a category stored in preferences from an older app version).
     */
    fun iconFor(categoryId: String): String =
        categories.firstOrNull { it.id == categoryId }?.icon ?: "📂"

    val words: List<Word> = buildList {
        addAll(animals)
        addAll(movies)
        addAll(tvSeries)
        addAll(books)
        addAll(countries)
        addAll(cities)
        addAll(sports)
        addAll(foods)
        addAll(music)
        addAll(landmarks)
        addAll(nature)
        addAll(science)
        addAll(technology)
        addAll(professions)
        addAll(space)
        addAll(mythology)
        addAll(geography)
        addAll(famousQuotes)
    }

    private val animals get() = listOf(
        Word("ARMADILLO", Difficulty.EASY, "Armored burrowing mammal", "animals"),
        Word("PANTHER", Difficulty.MEDIUM, "Spotted big cat", "animals"),
        Word("BUFFALO", Difficulty.MEDIUM, "Horned herd animal", "animals"),
        Word("FLAMINGO", Difficulty.EASY, "Pink wading bird", "animals"),
        Word("ALLIGATOR", Difficulty.EASY, "Swamp-dwelling reptile", "animals"),
        Word("CROCODILE", Difficulty.EASY, "Ancient river predator", "animals"),
        Word("PANGOLIN", Difficulty.MEDIUM, "Scaly anteater", "animals"),
        Word("ANTELOPE", Difficulty.EASY, "Graceful savannah herbivore", "animals"),
        Word("JAGUAR", Difficulty.MEDIUM, "Spotted Americas predator", "animals"),
        Word("TAPIR", Difficulty.HARD, "Short-trunked herbivore", "animals"),
        Word("ALPACA", Difficulty.MEDIUM, "Andean wool producer", "animals"),
        Word("MEERKAT", Difficulty.MEDIUM, "Sentinel of the desert", "animals"),
        Word("PORPOISE", Difficulty.MEDIUM, "Small toothed cetacean", "animals"),
    )

    private val movies get() = listOf(
        Word("AMIRKHAN", Difficulty.EASY, "Mr. Perfectionist of films", "movies"),
        Word("AVENGERS", Difficulty.EASY, "Marvel superhero team", "movies"),
        Word("TOYSTORY", Difficulty.EASY, "Pixar film about toys", "movies"),
        Word("DARKKNIGHT", Difficulty.EASY, "Gotham's caped crusader", "movies"),
        Word("GLADIATOR", Difficulty.EASY, "Roman arena epic", "movies"),
        Word("INCEPTION", Difficulty.EASY, "Dream heist thriller", "movies"),
        Word("INTERSTELLAR", Difficulty.EASY, "Wormhole space odyssey", "movies"),
        Word("CASABLANCA", Difficulty.EASY, "Wartime romance classic", "movies"),
        Word("GOODFELLAS", Difficulty.EASY, "Mob life drama", "movies"),
        Word("METROPOLIS", Difficulty.EASY, "Silent sci-fi landmark", "movies"),
        Word("OPPENHEIMER", Difficulty.EASY, "Atomic bomb biography epic", "movies"),
        Word("BOLLYWOOD", Difficulty.EASY, "Hindi film industry", "movies"),
        Word("PARASITE", Difficulty.MEDIUM, "Oscar-winning Korean thriller", "movies"),
        Word("PSYCHO", Difficulty.HARD, "Hitchcock's shower scene thriller", "movies"),
    )

    private val tvSeries get() = listOf(
        Word("YELLOWSTONE", Difficulty.EASY, "Modern ranch family saga", "tv_series"),
        Word("BREAKINGBAD", Difficulty.MEDIUM, "Walter White's meth empire drama", "tv_series"),
        Word("GAMEOTHRONES", Difficulty.HARD, "HBO's Westeros power struggle", "tv_series"),
        Word("STRANGERTHINGS", Difficulty.EASY, "Hawkins lab and the Upside Down", "tv_series"),
        Word("WEDNESDAY", Difficulty.MEDIUM, "Addams family academy student", "tv_series"),
        Word("SHERLOCK", Difficulty.HARD, "Baker Street detective drama", "tv_series"),
        Word("MONEYHEIST", Difficulty.EASY, "Spanish heist crime drama", "tv_series"),
        Word("CROWN", Difficulty.HARD, "Reign of Queen Elizabeth II", "tv_series"),
        Word("FRIENDS", Difficulty.EASY, "Central Perk gang sitcom", "tv_series"),
        Word("OFFICE", Difficulty.HARD, "Mockumentary workplace comedy", "tv_series"),
    )

    private val books get() = listOf(
        Word("SILMARILLION", Difficulty.EASY, "Tolkien's mythic history", "books"),
        Word("DRACULA", Difficulty.HARD, "Vampire count of Transylvania", "books"),
        Word("GULLIVER", Difficulty.MEDIUM, "Lilliputian travel satire", "books"),
        Word("MOBYDICK", Difficulty.EASY, "Ahab's white whale quest", "books"),
        Word("PRIDEANDPREJUDICE", Difficulty.EASY, "Austen's romantic novel", "books"),
        Word("GREATGATSBY", Difficulty.EASY, "Jazz Age millionaire tragedy", "books"),
        Word("WARANDPEACE", Difficulty.EASY, "Napoleonic Russia epic", "books"),
        Word("CRIMEANDPUNISHMENT", Difficulty.EASY, "Guilt and redemption in St Petersburg", "books"),
        Word("ALICEINWONDERLAND", Difficulty.EASY, "Curious girl's surreal adventure", "books"),
        Word("ODYSSEY", Difficulty.MEDIUM, "Homer's epic voyage home", "books"),
        Word("FRANKENSTEIN", Difficulty.MEDIUM, "Shelley's created monster", "books"),
        Word("LORDOFTHERINGS", Difficulty.EASY, "Fellowship of the ring quest", "books"),
        Word("HARRYPOTTER", Difficulty.EASY, "Boy wizard at Hogwarts", "books"),
        Word("HAMLET", Difficulty.HARD, "Danish prince's revenge tragedy", "books"),
        Word("MACBETH", Difficulty.HARD, "Scottish nobleman's ambition", "books"),
    )

    private val countries get() = listOf(
        Word("TIMORLESTE", Difficulty.EASY, "Young Southeast Asian republic", "countries"),
        Word("NIGERIA", Difficulty.MEDIUM, "West African giant", "countries"),
        Word("ETHIOPIA", Difficulty.MEDIUM, "Cradle of humanity", "countries"),
        Word("UGANDA", Difficulty.HARD, "Pearl of Africa", "countries"),
        Word("TANZANIA", Difficulty.MEDIUM, "Home of Kilimanjaro", "countries"),
        Word("ZIMBABWE", Difficulty.MEDIUM, "Land of Great Zimbabwe", "countries"),
        Word("COLOMBIA", Difficulty.MEDIUM, "South American coffee land", "countries"),
        Word("ARGENTINA", Difficulty.EASY, "Land of tango and gauchos", "countries"),
        Word("AUSTRALIA", Difficulty.EASY, "Island continent of unique wildlife", "countries"),
        Word("MADAGASCAR", Difficulty.EASY, "Island of lemurs", "countries"),
        Word("ICELAND", Difficulty.HARD, "Land of fire and ice", "countries"),
        Word("PORTUGAL", Difficulty.MEDIUM, "Iberian coastal nation", "countries"),
        Word("PHILIPPINES", Difficulty.EASY, "Seven-thousand-plus islands", "countries"),
    )

    private val cities get() = listOf(
        Word("AUSTIN", Difficulty.MEDIUM, "Live-music capital", "cities"),
        Word("MARRAKECH", Difficulty.EASY, "Moroccan red city", "cities"),
        Word("BARCELONA", Difficulty.EASY, "Catalan capital of art", "cities"),
        Word("AMSTERDAM", Difficulty.EASY, "Dutch canal capital", "cities"),
        Word("PRAGUE", Difficulty.HARD, "City of a hundred spires", "cities"),
        Word("VIENNA", Difficulty.HARD, "Austrian music capital", "cities"),
        Word("BERLIN", Difficulty.HARD, "German reunited capital", "cities"),
        Word("MUNICH", Difficulty.HARD, "Bavarian beer capital", "cities"),
        Word("VENICE", Difficulty.HARD, "Floating Italian city", "cities"),
        Word("MILAN", Difficulty.HARD, "Italian fashion capital", "cities"),
        Word("MOSCOW", Difficulty.HARD, "Russian red-square capital", "cities"),
        Word("BEIJING", Difficulty.HARD, "Chinese imperial capital", "cities"),
        Word("SHANGHAI", Difficulty.EASY, "China's megacity port", "cities"),
    )

    private val sports get() = listOf(
        Word("WEIGHTLIFTING", Difficulty.EASY, "Snatch and clean discipline", "sports"),
        Word("VOLLEYBALL", Difficulty.EASY, "Net-and-spike team sport", "sports"),
        Word("HANDBALL", Difficulty.HARD, "Indoor throwing sport", "sports"),
        Word("BADMINTON", Difficulty.EASY, "Played with a shuttlecock", "sports"),
        Word("BASEBALL", Difficulty.EASY, "Bat-and-ball American pastime", "sports"),
        Word("FOOTBALL", Difficulty.EASY, "Gridiron American sport", "sports"),
        Word("SOCCER", Difficulty.HARD, "World's most popular sport", "sports"),
        Word("SURFING", Difficulty.HARD, "Ocean wave riding", "sports"),
        Word("SKIING", Difficulty.HARD, "Snow slope racing", "sports"),
        Word("BOXING", Difficulty.HARD, "Fought in a ring", "sports"),
        Word("RUGBY", Difficulty.HARD, "Oval ball contact sport", "sports"),
        Word("TENNIS", Difficulty.HARD, "Racket sport with a net", "sports"),
        Word("HOCKEY", Difficulty.HARD, "Played with sticks and a puck", "sports"),
        Word("BASKETBALL", Difficulty.MEDIUM, "Slam-dunk indoor court game", "sports"),
    )

    private val foods get() = listOf(
        Word("CURRY", Difficulty.HARD, "Turmeric-laced sauce", "foods"),
        Word("SUSHI", Difficulty.HARD, "Japanese vinegared rice dish", "foods"),
        Word("RAMEN", Difficulty.HARD, "Japanese noodle soup", "foods"),
        Word("TACOS", Difficulty.HARD, "Mexican folded tortillas", "foods"),
        Word("BURGER", Difficulty.HARD, "Grilled patty sandwich", "foods"),
        Word("STEAK", Difficulty.HARD, "Grilled beef cut", "foods"),
        Word("CHEESECAKE", Difficulty.EASY, "Creamy baked dessert", "foods"),
        Word("CROISSANT", Difficulty.EASY, "Flaky French pastry", "foods"),
        Word("CHOCOLATE", Difficulty.EASY, "Cocoa confection", "foods"),
        Word("SPAGHETTI", Difficulty.EASY, "Long thin Italian pasta", "foods"),
        Word("GUACAMOLE", Difficulty.EASY, "Avocado based dip", "foods"),
        Word("BRUSCHETTA", Difficulty.EASY, "Toasted Italian starter", "foods"),
        Word("CAPPUCCINO", Difficulty.EASY, "Frothy Italian coffee", "foods"),
        Word("PIZZA", Difficulty.MEDIUM, "Italian flatbread with toppings", "foods"),
    )

    private val music get() = listOf(
        Word("LOUISARMSTRONG", Difficulty.EASY, "Satchmo of jazz", "music"),
        Word("SYMPHONY", Difficulty.MEDIUM, "Multi-movement orchestral work", "music"),
        Word("SAXOPHONE", Difficulty.EASY, "Reeded brass instrument", "music"),
        Word("PERCUSSION", Difficulty.EASY, "Struck instrument family", "music"),
        Word("HARMONY", Difficulty.MEDIUM, "Notes sounded together", "music"),
        Word("TRUMPET", Difficulty.MEDIUM, "Valved brass instrument", "music"),
        Word("VIOLIN", Difficulty.HARD, "Bowed string instrument", "music"),
        Word("FLUTE", Difficulty.HARD, "Blown woodwind", "music"),
        Word("GUITAR", Difficulty.HARD, "Six-stringed instrument", "music"),
        Word("PIANO", Difficulty.HARD, "Eighty-eight keys", "music"),
        Word("SITAR", Difficulty.HARD, "Stringed Indian lute", "music"),
        Word("TABLA", Difficulty.HARD, "Pair of Indian hand drums", "music"),
        Word("VEENA", Difficulty.HARD, "Stringed classical instrument", "music"),
    )

    private val landmarks get() = listOf(
        Word("CAPECANAVERAL", Difficulty.EASY, "NASA launch site", "landmarks"),
        Word("COLOSSEUM", Difficulty.EASY, "Roman amphitheatre", "landmarks"),
        Word("STONEHENGE", Difficulty.EASY, "Prehistoric stone circle", "landmarks"),
        Word("ACROPOLIS", Difficulty.EASY, "Athenian hilltop citadel", "landmarks"),
        Word("PARTHENON", Difficulty.EASY, "Temple to Athena", "landmarks"),
        Word("MACHUPICCHU", Difficulty.EASY, "Incan mountain citadel", "landmarks"),
        Word("TAJMAHAL", Difficulty.MEDIUM, "White-marble monument of love", "landmarks"),
        Word("GOLDENTEMPLE", Difficulty.EASY, "Harmandir Sahib at Amritsar", "landmarks"),
        Word("MEENAKSHI", Difficulty.EASY, "Madurai's towering temple", "landmarks"),
        Word("KHAJURAHO", Difficulty.EASY, "Temples famous for art carvings", "landmarks"),
        Word("KANCHIPURAM", Difficulty.EASY, "Thousand-temple town in Tamil Nadu", "landmarks"),
        Word("MAHABALIPURAM", Difficulty.EASY, "Shore temple by the Bay of Bengal", "landmarks"),
        Word("GRANDCANYON", Difficulty.EASY, "Mile-deep red-rock gorge", "landmarks"),
        Word("MOUNTEVEREST", Difficulty.HARD, "World's highest peak", "landmarks"),
    )

    private val nature get() = listOf(
        Word("AVALANCHE", Difficulty.EASY, "Rushing mass of snow", "nature"),
        Word("WATERFALL", Difficulty.EASY, "Water falling from height", "nature"),
        Word("RAINFOREST", Difficulty.EASY, "Dense tropical woodland", "nature"),
        Word("ARCHIPELAGO", Difficulty.EASY, "Chain of islands", "nature"),
        Word("PENINSULA", Difficulty.EASY, "Land surrounded on three sides", "nature"),
        Word("GLACIER", Difficulty.MEDIUM, "Slow-moving ice mass", "nature"),
        Word("MOUNTAIN", Difficulty.MEDIUM, "Tall natural elevation", "nature"),
        Word("RAINBOW", Difficulty.MEDIUM, "Arc of refracted light", "nature"),
        Word("RIVER", Difficulty.HARD, "Flowing watercourse", "nature"),
        Word("OCEAN", Difficulty.HARD, "Vast body of saltwater", "nature"),
        Word("FLOWER", Difficulty.HARD, "Blooming plant part", "nature"),
        Word("FOREST", Difficulty.HARD, "Dense stand of trees", "nature"),
        Word("CANYON", Difficulty.HARD, "Deep river-carved gorge", "nature"),
    )

    private val science get() = listOf(
        Word("MITOCHONDRIA", Difficulty.EASY, "Powerhouse of the cell", "science"),
        Word("CHROMOSOME", Difficulty.EASY, "Structure carrying genes", "science"),
        Word("PHOTOSYNTHESIS", Difficulty.EASY, "Plants converting light to energy", "science"),
        Word("THERMODYNAMICS", Difficulty.EASY, "Study of heat and energy", "science"),
        Word("NEUTRON", Difficulty.MEDIUM, "Uncharged nuclear particle", "science"),
        Word("MOLECULE", Difficulty.MEDIUM, "Bonded group of atoms", "science"),
        Word("ELECTRON", Difficulty.MEDIUM, "Negatively charged particle", "science"),
        Word("ENZYME", Difficulty.HARD, "Biological catalyst", "science"),
        Word("ENERGY", Difficulty.HARD, "Capacity to do work", "science"),
        Word("GRAVITY", Difficulty.MEDIUM, "Force pulling masses together", "science"),
        Word("QUANTUM", Difficulty.HARD, "Smallest discrete unit of matter", "science"),
        Word("PLASMA", Difficulty.HARD, "Fourth state of matter", "science"),
        Word("VACCINE", Difficulty.MEDIUM, "Disease prevention preparation", "science"),
        Word("ECOLOGY", Difficulty.EASY, "Study of ecosystems", "science"),
    )

    private val technology get() = listOf(
        Word("VIRTUALIZATION", Difficulty.EASY, "Running simulated machines", "technology"),
        Word("ALGORITHM", Difficulty.EASY, "Step-by-step procedure", "technology"),
        Word("ENCRYPTION", Difficulty.EASY, "Encoding data for secrecy", "technology"),
        Word("ARCHITECTURE", Difficulty.EASY, "High-level system design", "technology"),
        Word("ANDROID", Difficulty.MEDIUM, "Google's mobile platform", "technology"),
        Word("COMPOSE", Difficulty.MEDIUM, "Declarative UI toolkit", "technology"),
        Word("NETWORK", Difficulty.MEDIUM, "Connected computer system", "technology"),
        Word("DATABASE", Difficulty.MEDIUM, "Structured data store", "technology"),
        Word("CLOUD", Difficulty.HARD, "Remote computing resource", "technology"),
        Word("ROBOT", Difficulty.HARD, "Programmable machine", "technology"),
        Word("KOTLIN", Difficulty.HARD, "Modern JVM language", "technology"),
        Word("ARTIFICIALINTELLIGENCE", Difficulty.EASY, "Machine simulation of human smarts", "technology"),
        Word("BLOCKCHAIN", Difficulty.MEDIUM, "Distributed ledger technology", "technology"),
        Word("CYBERSECURITY", Difficulty.HARD, "Digital system protection", "technology"),
    )

    private val professions get() = listOf(
        Word("TURING", Difficulty.MEDIUM, "Computing pioneer", "professions"),
        Word("SCIENTIST", Difficulty.EASY, "Conducts research", "professions"),
        Word("JOURNALIST", Difficulty.EASY, "Reports the news", "professions"),
        Word("VETERINARIAN", Difficulty.EASY, "Treats animals", "professions"),
        Word("PHOTOGRAPHER", Difficulty.EASY, "Captures images", "professions"),
        Word("ANTHROPOLOGIST", Difficulty.EASY, "Studies human societies", "professions"),
        Word("PHARMACIST", Difficulty.EASY, "Dispenses medicine", "professions"),
        Word("ARCHITECT", Difficulty.EASY, "Designs buildings", "professions"),
        Word("ROOSEVELT", Difficulty.EASY, "New Deal president", "professions"),
        Word("WASHINGTON", Difficulty.EASY, "The nation's founder", "professions"),
        Word("RAJIVGANDHI", Difficulty.EASY, "Former Prime Minister of India", "professions"),
        Word("NARENDRAMODI", Difficulty.EASY, "Current Prime Minister", "professions"),
        Word("ENGINEER", Difficulty.MEDIUM, "Designs and builds systems", "professions"),
        Word("SURGEON", Difficulty.HARD, "Performs surgical operations", "professions"),
    )

    private val space get() = listOf(
        Word("COSMOLOGY", Difficulty.EASY, "Study of the universe's origin", "space"),
        Word("CONSTELLATION", Difficulty.EASY, "Named pattern of stars", "space"),
        Word("ASTRONAUT", Difficulty.EASY, "Traveller in space", "space"),
        Word("EXOPLANET", Difficulty.EASY, "Planet outside our solar system", "space"),
        Word("TELESCOPE", Difficulty.EASY, "Instrument for viewing space", "space"),
        Word("SUPERNOVA", Difficulty.EASY, "Exploding star", "space"),
        Word("ASTEROID", Difficulty.MEDIUM, "Rocky body orbiting the sun", "space"),
        Word("NEPTUNE", Difficulty.MEDIUM, "Furthest official planet", "space"),
        Word("JUPITER", Difficulty.MEDIUM, "Largest planet", "space"),
        Word("MARS", Difficulty.HARD, "The red planet", "space"),
        Word("MOON", Difficulty.HARD, "Earth's natural satellite", "space"),
        Word("STAR", Difficulty.HARD, "Burning ball of gas", "space"),
        Word("VENUS", Difficulty.HARD, "Second planet from the sun", "space"),
        Word("COMET", Difficulty.HARD, "Icy body with a tail", "space"),
        Word("PLUTO", Difficulty.HARD, "Former ninth planet, now dwarf", "space"),
        Word("ORBIT", Difficulty.HARD, "Path around a star or planet", "space"),
        Word("SATURN", Difficulty.HARD, "Planet famous for rings", "space"),
        Word("GALAXY", Difficulty.HARD, "Vast system of stars", "space"),
        Word("NEBULA", Difficulty.HARD, "Interstellar cloud of gas", "space"),
    )

    private val mythology get() = listOf(
        Word("PERSEPHONE", Difficulty.EASY, "Queen of the underworld", "mythology"),
        Word("PROMETHEUS", Difficulty.EASY, "Titan who gave fire to humans", "mythology"),
        Word("APHRODITE", Difficulty.EASY, "Greek goddess of love", "mythology"),
        Word("POSEIDON", Difficulty.MEDIUM, "God of the sea", "mythology"),
        Word("MINOTAUR", Difficulty.MEDIUM, "Bull-headed creature in the labyrinth", "mythology"),
        Word("ATLAS", Difficulty.HARD, "Titan who holds the sky", "mythology"),
        Word("APOLLO", Difficulty.HARD, "God of the sun and music", "mythology"),
        Word("ATHENA", Difficulty.HARD, "Greek goddess of wisdom", "mythology"),
        Word("MEDUSA", Difficulty.HARD, "Gorgon with serpent hair", "mythology"),
        Word("OSIRIS", Difficulty.HARD, "Egyptian god of the afterlife", "mythology"),
        Word("HERMES", Difficulty.HARD, "Winged messenger god", "mythology"),
        Word("ZEUS", Difficulty.HARD, "King of the Greek gods", "mythology"),
        Word("THOR", Difficulty.HARD, "Norse god of thunder", "mythology"),
        Word("HERA", Difficulty.HARD, "Greek queen of the gods", "mythology"),
        Word("ARES", Difficulty.HARD, "Greek god of war", "mythology"),
    )

    private val geography get() = listOf(
        Word("VIRGINIA", Difficulty.EASY, "Old Dominion", "geography"),
        Word("HIMALAYAS", Difficulty.EASY, "Highest mountain range on Earth", "geography"),
        Word("MEDITERRANEAN", Difficulty.EASY, "Sea surrounded by Europe and Africa", "geography"),
        Word("KILIMANJARO", Difficulty.EASY, "Highest peak in Africa", "geography"),
        Word("APPALACHIAN", Difficulty.EASY, "Ancient eastern US mountain range", "geography"),
        Word("BRAHMAPUTRA", Difficulty.EASY, "River through Assam", "geography"),
        Word("SUNDARBANS", Difficulty.EASY, "Tidal mangrove and tiger delta", "geography"),
        Word("RAJASTHAN", Difficulty.EASY, "Land of kings", "geography"),
        Word("TAMILNADU", Difficulty.EASY, "Southernmost Indian state", "geography"),
        Word("MAHARASHTRA", Difficulty.EASY, "State holding Mumbai", "geography"),
        Word("CALIFORNIA", Difficulty.EASY, "Golden State", "geography"),
        Word("MINNESOTA", Difficulty.EASY, "Land of ten thousand lakes", "geography"),
        Word("PENNSYLVANIA", Difficulty.EASY, "Keystone State", "geography"),
        Word("EVEREST", Difficulty.MEDIUM, "Highest mountain on Earth", "geography"),
        Word("PACIFIC", Difficulty.MEDIUM, "World's largest ocean", "geography"),
        Word("ATLANTIC", Difficulty.MEDIUM, "Ocean between Americas and Europe", "geography"),
        Word("GUJARAT", Difficulty.MEDIUM, "Homeland of Mahatma Gandhi", "geography"),
        Word("FLORIDA", Difficulty.MEDIUM, "Sunshine State", "geography"),
        Word("COLORADO", Difficulty.MEDIUM, "Rocky Mountain state", "geography"),
        Word("ARIZONA", Difficulty.MEDIUM, "Grand Canyon state", "geography"),
        Word("MONTANA", Difficulty.MEDIUM, "Treasure State", "geography"),
        Word("WYOMING", Difficulty.MEDIUM, "Cowboy State", "geography"),
        Word("GEORGIA", Difficulty.MEDIUM, "Peach State", "geography"),
        Word("ILLINOIS", Difficulty.MEDIUM, "Land of Lincoln", "geography"),
        Word("MICHIGAN", Difficulty.MEDIUM, "Great Lakes state", "geography"),
        Word("MISSOURI", Difficulty.MEDIUM, "Show-Me state", "geography"),
        Word("SAHARA", Difficulty.HARD, "World's largest hot desert", "geography"),
        Word("AMAZON", Difficulty.HARD, "Vast South American rainforest river", "geography"),
        Word("GANGES", Difficulty.HARD, "Sacred river of India", "geography"),
        Word("DECCAN", Difficulty.HARD, "Southern Indian plateau", "geography"),
        Word("KERALA", Difficulty.HARD, "God's own state", "geography"),
        Word("PUNJAB", Difficulty.HARD, "Land of five rivers", "geography"),
        Word("TEXAS", Difficulty.HARD, "Lone Star State", "geography"),
        Word("HAWAII", Difficulty.HARD, "Aloha State", "geography"),
        Word("ALASKA", Difficulty.HARD, "Last Frontier", "geography"),
        Word("NEVADA", Difficulty.HARD, "Battle-Born State", "geography"),
        Word("OREGON", Difficulty.HARD, "Beaver State", "geography"),
        Word("KANSAS", Difficulty.HARD, "Tornado Alley heartland", "geography"),
        Word("NILE", Difficulty.HARD, "Longest river in the world", "geography"),
        Word("ALPS", Difficulty.HARD, "European mountain range", "geography"),
        Word("THAR", Difficulty.HARD, "India's northwestern desert", "geography"),
        Word("UTAH", Difficulty.HARD, "Beehive State", "geography"),
    )

    private val famousQuotes get() = listOf(
        Word("VERSAILLES PALACE", Difficulty.EASY, "French royal landmark", "famous_quotes"),
        Word("FIGHTCLUB", Difficulty.EASY, "First rule reference", "famous_quotes"),
        Word("STATUEOFLIBERTY", Difficulty.EASY, "New York harbour monument", "famous_quotes"),
        Word("EIFFELTOWER", Difficulty.MEDIUM, "Paris iron lattice tower", "famous_quotes"),
        Word("GREATWALL", Difficulty.MEDIUM, "Ancient Chinese fortification", "famous_quotes"),
        Word("COLOSSEUM", Difficulty.EASY, "Roman amphitheatre landmark", "famous_quotes"),
        Word("TAJMAHAL", Difficulty.MEDIUM, "Agra marble mausoleum", "famous_quotes"),
        Word("PYRAMIDS", Difficulty.HARD, "Egyptian ancient tombs", "famous_quotes"),
        Word("MOUNT EVEREST", Difficulty.HARD, "World's highest peak", "famous_quotes"),
        Word("MACHUPICCHU", Difficulty.EASY, "Incan mountain citadel", "famous_quotes"),
        Word("STONEHENGE", Difficulty.EASY, "Prehistoric stone circle", "famous_quotes"),
        Word("SYDNEYOPERAHOUSE", Difficulty.MEDIUM, "Australian shell-shaped venue", "famous_quotes"),
        Word("BIGBEN", Difficulty.HARD, "London clock tower", "famous_quotes"),
        Word("STATUEOFLIBERTY", Difficulty.EASY, "New York harbour monument", "famous_quotes"),
        Word("WHITEHOUSE", Difficulty.MEDIUM, "US presidential residence", "famous_quotes"),
    )

}
