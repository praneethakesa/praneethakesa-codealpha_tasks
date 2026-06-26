package com.example.data

data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val category: String, // "Wisdom", "Motivation", "Science", "Art", "Custom"
    val isCustom: Boolean = false
)

object QuoteProvider {
    val initialQuotes = listOf(
        Quote("q1", "The only way to do great work is to love what you do.", "Steve Jobs", "Motivation"),
        Quote("q2", "Be yourself; everyone else is already taken.", "Oscar Wilde", "Wisdom"),
        Quote("q3", "In the middle of difficulty lies opportunity.", "Albert Einstein", "Science"),
        Quote("q4", "Waste no more time arguing about what a good man should be. Be one.", "Marcus Aurelius", "Wisdom"),
        Quote("q5", "The mind is not a vessel to be filled, but a fire to be kindled.", "Plutarch", "Wisdom"),
        Quote("q6", "He who has a why to live for can bear almost any how.", "Friedrich Nietzsche", "Wisdom"),
        Quote("q7", "I have not failed. I've just found 10,000 ways that won't work.", "Thomas A. Edison", "Science"),
        Quote("q8", "Creativity is intelligence having fun.", "Albert Einstein", "Art"),
        Quote("q9", "It always seems impossible until it's done.", "Nelson Mandela", "Motivation"),
        Quote("q10", "The journey of a thousand miles begins with one step.", "Lao Tzu", "Wisdom"),
        Quote("q11", "What we achieve inwardly will change outer reality.", "Plutarch", "Wisdom"),
        Quote("q12", "We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Aristotle", "Wisdom"),
        Quote("q13", "The only true wisdom is in knowing you know nothing.", "Socrates", "Wisdom"),
        Quote("q14", "Art is the lie that enables us to realize the truth.", "Pablo Picasso", "Art"),
        Quote("q15", "Science is organized knowledge. Wisdom is organized life.", "Immanuel Kant", "Science"),
        Quote("q16", "The best way to predict your future is to create it.", "Abraham Lincoln", "Motivation"),
        Quote("q17", "Happiness depends upon ourselves.", "Aristotle", "Wisdom"),
        Quote("q18", "Life is what happens when you're busy making other plans.", "John Lennon", "Wisdom"),
        Quote("q19", "Do what you can, with what you have, where you are.", "Theodore Roosevelt", "Motivation"),
        Quote("q20", "Act as if what you do makes a difference. It does.", "William James", "Motivation"),
        Quote("q21", "Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill", "Motivation"),
        Quote("q22", "The starry heaven above me and the moral law within me.", "Immanuel Kant", "Wisdom"),
        Quote("q23", "Nature does not hurry, yet everything is accomplished.", "Lao Tzu", "Wisdom"),
        Quote("q24", "If I have seen further it is by standing on the shoulders of Giants.", "Isaac Newton", "Science"),
        Quote("q25", "To be yourself in a world that is constantly trying to make you something else is the greatest accomplishment.", "Ralph Waldo Emerson", "Wisdom"),
        Quote("q26", "Everything you can imagine is real.", "Pablo Picasso", "Art"),
        Quote("q27", "Nothing in life is to be feared, it is only to be understood. Now is the time to understand more, so that we may fear less.", "Marie Curie", "Science"),
        Quote("q28", "The unexamined life is not worth living.", "Socrates", "Wisdom"),
        Quote("q29", "Happiness is not something ready made. It comes from your own actions.", "Dalai Lama", "Wisdom"),
        Quote("q30", "All limits are self-imposed.", "Unknown", "Motivation"),
        Quote("q31", "There is no exquisite beauty without some strangeness in the proportion.", "Edgar Allan Poe", "Art"),
        Quote("q32", "We do not inherit the earth from our ancestors, we borrow it from our children.", "Antoine de Saint-Exupéry", "Wisdom"),
        Quote("q33", "The important thing is not to stop questioning. Curiosity has its own reason for existence.", "Albert Einstein", "Science"),
        Quote("q34", "You must be the change you wish to see in the world.", "Mahatma Gandhi", "Wisdom"),
        Quote("q35", "The soul becomes dyed with the color of its thoughts.", "Marcus Aurelius", "Wisdom"),
        Quote("q36", "Knowing yourself is the beginning of all wisdom.", "Aristotle", "Wisdom"),
        Quote("q37", "That which does not kill us makes us stronger.", "Friedrich Nietzsche", "Wisdom"),
        Quote("q38", "Simplicity is the ultimate sophistication.", "Leonardo da Vinci", "Art"),
        Quote("q39", "Your time is limited, so don't waste it living someone else's life.", "Steve Jobs", "Motivation"),
        Quote("q40", "If you want to live a happy life, tie it to a goal, not to people or things.", "Albert Einstein", "Motivation"),
        Quote("q41", "Difficulties strengthen the mind, as labor does the body.", "Seneca", "Wisdom"),
        Quote("q42", "An unexamined life is a lost opportunity.", "Plato", "Wisdom"),
        Quote("q43", "Where there is love there is life.", "Mahatma Gandhi", "Wisdom"),
        Quote("q44", "Learn from yesterday, live for today, hope for tomorrow.", "Albert Einstein", "Science"),
        Quote("q45", "The work of art is a scream of freedom.", "Christo", "Art"),
        Quote("q46", "Don't count the days, make the days count.", "Muhammad Ali", "Motivation"),
        Quote("q47", "It is the mark of an educated mind to be able to entertain a thought without accepting it.", "Aristotle", "Wisdom"),
        Quote("q48", "Invention is the mother of necessity.", "Unknown", "Science"),
        Quote("q49", "Art is a collaboration between God and the artist, and the less the artist does the better.", "André Gide", "Art"),
        Quote("q50", "Begin at once to live, and count each separate day as a separate life.", "Seneca", "Wisdom")
    )
}
