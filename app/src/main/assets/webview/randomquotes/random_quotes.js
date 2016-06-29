/*
 * Copyright 2016 Johann Reyes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Magic Mirror Module: random_quotes
 * v1.0 - June 2016
 *
 * By Ashley M. Kirchner <kirash4@gmail.com>
 * Beer Licensed (meaning, if you like this module, feel free to have a beer on me, or send me one.)
 */

Module.register("random_quotes",{

    /* Quotes are courtesy of BrainyQuote.com
     There is no 'automated' way to fetch random quotes from BrainyQuote.com. You'll have to
     manually do that yourself. Or find a free random quote API at which point you are welcome
     to rewrite this module to use that. All the ones I've found are paid services. Free ones
     only allow a single quote for the day. Kinda defeats the purpose.
     */

    // Module config defaults.
    defaults: {
        updateInterval: 300,	// Value is in SECONDS
        fadeSpeed: 4,			// How fast (in SECONDS) to fade out and back in when changing quotes
        category: 'random',		// Category to use
        quotes: {
            inspirational: [
                "Your big opportunity may be right where you are now. ~ Napoleon Hill",
                "Hope is but the dream of those who wake. ~ Matthew Prior",
                "Men must live and create. Live to the point of tears. ~ Albert Camus",
                "Try to be a rainbow in someone's cloud. ~ Maya Angelou",
                "I think that the reason for my success is that I am really not aspirational. I am inspirational in that the people at home feel like they can really relate to me. ~ Rosie O'Donnell",
                "In fact, I wouldn't really call this a Gospel album, I call it more an inspirational album. ~ Smokey Robinson",
                "Act like you expect to get into the end zone. ~ Christopher Morley",
                "A compliment is something like a kiss through a veil. ~ Victor Hugo",
                "Every heart that has beat strongly and cheerfully has left a hopeful impulse behind it in the world, and bettered the tradition of mankind. ~ Robert Louis Stevenson",
                "I have always believed, and I still believe, that whatever good or bad fortune may come our way we can always give it meaning and transform it into something of value. ~ Hermann Hesse",
            ],
            life: [
                "A real man loves his wife, and places his family as the most important thing in life. Nothing has brought me more peace and content in life than simply being a good husband and father. ~ Frank Abagnale",
                "I don't know why his lawyers didn't tell him, 'You don't have to answer any questions about your private life, Mr. President. Let them sue you. Take the heat. You don't have to answer.' ~ Chris Matthews",
                "Life is full of beauty. Notice it. Notice the bumble bee, the small child, and the smiling faces. Smell the rain, and feel the wind. Live your life to the fullest potential, and fight for your dreams. ~ Ashley Smith",
                "For me life is continuously being hungry. The meaning of life is not simply to exist, to survive, but to move ahead, to go up, to achieve, to conquer. ~ Arnold Schwarzenegger",
                "Without deep reflection one knows from daily life that one exists for other people. ~ Albert Einstein",
                "To me, having kids is the ultimate job in life. I want to be most successful at being a good father. ~ Nick Lachey",
                "I believe that I was a dog in a past life. That's the only thing that would explain why I like to snack on Purina Dog Chow. ~ Dean Koontz",
                "To be idle is a short road to death and to be diligent is a way of life; foolish people are idle, wise people are diligent. ~ Buddha",
                "We can't plan life. All we can do is be available for it. ~ Lauryn Hill",
                "Friends are as companions on a journey, who ought to aid each other to persevere in the road to a happier life. ~ Pythagoras",
            ],
            love: [
                "Men always want to be a woman's first love - women like to be a man's last romance. ~ Oscar Wilde",
                "Throw your dreams into space like a kite, and you do not know what it will bring back, a new life, a new friend, a new love, a new country. ~ Anais Nin",
                "I'm really proud to be Filipino. Filipinos are really supportive, and I want to thank all of them. I love them! ~ Charice Pempengco",
                "Some think love can be measured by the amount of butterflies in their tummy. Others think love can be measured in bunches of flowers, or by using the words 'for ever.' But love can only truly be measured by actions. It can be a small thing, such as peeling an orange for a person you love because you know they don't like doing it. ~ Marian Keyes",
                "I would love to be a father. I had a great father who taught me how gratifying that is. I'm not going to deny myself that. I think I'd be good at it. Everybody wants that experience. I definitely do. ~ Mike Myers",
                "In the end, the love you take is equal to the love you make. ~ Paul McCartney",
                "I love argument, I love debate. I don't expect anyone just to sit there and agree with me, that's not their job. ~ Margaret Thatcher",
                "Grief is the price we pay for love. ~ Queen Elizabeth II",
                "We waste time looking for the perfect lover, instead of creating the perfect love. ~ Tom Robbins",
                "True love comes quietly, without banners or flashing lights. If you hear bells, get your ears checked. ~ Erich Segal",
            ],
            motivational: [
                "Be kind whenever possible. It is always possible. ~ Dalai Lama",
                "You are never too old to set another goal or to dream a new dream. ~ C. S. Lewis",
                "Get action. Seize the moment. Man was never intended to become an oyster. ~ Theodore Roosevelt",
                "The people who influence you are the people who believe in you. ~ Henry Drummond",
                "The first question which the priest and the Levite asked was: 'If I stop to help this man, what will happen to me?' But... the good Samaritan reversed the question: 'If I do not stop to help this man, what will happen to him?' ~ Martin Luther King, Jr.",
                "We should not give up and we should not allow the problem to defeat us. ~ A. P. J. Abdul Kalam",
                "You can't cross the sea merely by standing and staring at the water. ~ Rabindranath Tagore",
                "Leap, and the net will appear. ~ John Burroughs",
                "Do your work with your whole heart, and you will succeed - there's so little competition. ~ Elbert Hubbard",
                "Do not wait to strike till the iron is hot; but make it hot by striking. ~ William Butler Yeats",
            ],
            positive: [
                "I faced quite a few challenging times, and in front of those, I was more positive than some people not facing those conditions. I'm actually of the belief now that it is that struggle that offers you that open-hearted hope. ~ K'naan",
                "So there was a fire inside me. And that fire inside you, it can be turned into a negative form or a positive form. And I gradually realised that I had this fire and that it had to be used in a positive way. ~ John Newcombe",
                "I can walk into a room and create a good ambience. I was taught all about this back when I studied acting. One of the things they would teach you is how to send out positive signals when you enter a room. I am glad I learned this. ~ Jean Reno",
                "Very gifted people, they win and they win, and they are told that they win because they are a winner. That seems like a positive thing to tell children, but ultimately, what that means is when they lose, it must make them a loser. ~ Joshua Waitzkin",
                "I collect crystals and gemstones, and I've been collecting them since I was a little girl. They give me positive energy and strength. They make me feel connected to the earth. I cherish them. ~ Isabel Lucas",
                "Failure is enriching. It's also important to accept that you'll make mistakes - it's how you build your expertise. The trick is to learn a positive lesson from all of life's negative moments. ~ Alain Ducasse",
                "So, we have choice, and sometimes it seems very hard, but the best way to heal physically or emotionally is to keep positive. ~ Petra Nemcova",
                "Say and do something positive that will help the situation; it doesn't take any brains to complain. ~ Robert A. Cook",
                "Everyone at home is so supportive. People recognise me, say how proud they are of me. It's awesome to hear, it's amazing to know I can touch so many people in a positive way. ~ Kirsty Coventry",
                "Electricity is of two kinds, positive and negative. The difference is, I presume, that one comes a little more expensive, but is more durable; the other is a cheaper thing, but the moths get into it. ~ Stephen Leacock",
            ],
            success: [
                "The road to success is always under construction. ~ Lily Tomlin",
                "The first step toward success is taken when you refuse to be a captive of the environment in which you first find yourself. ~ Mark Caine",
                "I mean, we are tribal by nature, and sometimes success and material wealth can divide and separate - it's not a new philosophy I'm sharing - more than hardship, hardship tends to unify. ~ Colin Farrell",
                "Nothing is as seductive as the assurance of success. ~ Gertrude Himmelfarb",
                "Good planning is important. I've also regarded a sense of humor as one of the most important things on a big expedition. When you're in a difficult or dangerous situation, or when you're depressed about the chances of success, someone who can make you laugh eases the tension. ~ Edmund Hillary",
                "I'll tell you, there is nothing better in life than being a late bloomer. I believe that success can happen at any time and at any age. ~ Salma Hayek",
                "My definition of success is to live your life in a way that causes you to feel a ton of pleasure and very little pain - and because of your lifestyle, have the people around you feel a lot more pleasure than they do pain. ~ Tony Robbins",
                "No one who achieves success does so without acknowledging the help of others. The wise and confident acknowledge this help with gratitude. ~ Alfred North Whitehead",
                "Success is not final, failure is not fatal: it is the courage to continue that counts. ~ Winston Churchill",
                "Success is finding satisfaction in giving a little more than you take. ~ Christopher Reeve",
            ]
        },
    },


    // Define start sequence.
    start: function() {
        Log.info("Starting module: " + this.name);

        this.lastQuoteIndex = -1;

        // Schedule update timer.
        var self = this;
        setInterval(function() {
            self.updateDom(self.config.fadeSpeed * 1000);
        }, this.config.updateInterval * 1000);
    },

    /* randomIndex(quotes)
     * Generate a random index for a list of quotes.
     *
     * argument quotes Array<String> - Array with quotes.
     *
     * return Number - Random index.
     */
    randomIndex: function(quotes) {
        if (quotes.length === 1) {
            return 0;
        }

        var generate = function() {
            return Math.floor(Math.random() * quotes.length);
        };

        var quoteIndex = generate();

        while (quoteIndex === this.lastQuoteIndex) {
            quoteIndex = generate();
        }

        this.lastQuoteIndex = quoteIndex;

        return quoteIndex;
    },

    /* quoteArray()
     * Retrieve an array of quotes for the time of the day.
     *
     * return quotes Array<String> - Array with quotes for the time of the day.
     */
    quoteArray: function() {
        if (this.config.category == 'random') {
            return this.config.quotes[Object.keys(this.config.quotes)[Math.floor(Math.random() * Object.keys(this.config.quotes).length)]];
        } else {
            return this.config.quotes[this.config.category];
        }
    },

    /* randomQuote()
     * Retrieve a random quote.
     *
     * return quote string - A quote.
     */
    randomQuote: function() {
        var quotes = this.quoteArray();
        var index = this.randomIndex(quotes);
        return quotes[index].split(" ~ ");
    },

    // Override dom generator.
    getDom: function() {
        var quoteText = this.randomQuote();

        var qMsg = quoteText[0];
        var qAuthor = quoteText[1];

        var wrapper = document.createElement("div");

        var quote = document.createElement("div");
        quote.className = "bright medium light";
        quote.style.textAlign = 'center';
        quote.style.margin = '0 auto';
        quote.style.maxWidth = '50%';
        quote.innerHTML = qMsg;

        wrapper.appendChild(quote);

        var author = document.createElement("div");
        author.className = "light small dimmed";
        author.innerHTML = "~ " + qAuthor;

        wrapper.appendChild(author);

        return wrapper;
    }

});