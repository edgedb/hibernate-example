package com.edgedb.samples.hibernate;

import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edgedb.samples.hibernate.carrot.Carrot;
import com.edgedb.samples.hibernate.carrot.CarrotRepo;
import com.edgedb.samples.hibernate.fox.Fox;
import com.edgedb.samples.hibernate.fox.FoxRepo;
import com.edgedb.samples.hibernate.position.Position;
import com.edgedb.samples.hibernate.position.PositionRepo;
import com.edgedb.samples.hibernate.rabbit.Rabbit;
import com.edgedb.samples.hibernate.rabbit.RabbitRepo;

@Service
public class SpawnService {

    @Autowired
    RabbitRepo rabbitRepo;

    @Autowired
    CarrotRepo carrotRepo;

    @Autowired
    FoxRepo foxRepo;

    @Autowired
    PositionRepo positionRepo;

    public void tick() {
        var rabbitCount = rabbitRepo.countByPositionIsNotNull();
        if (rabbitCount == 0) {
            return;
        }

        var carrotCount = carrotRepo.count();

        var foxCount = foxRepo.countByPositionIsNotNull();

        List<Rabbit> rabbits = null;

        // spawn a carrot
        if (Math.random() < carrotSpawnProb(rabbitCount, carrotCount)) {
            rabbits = rabbitRepo.findByPositionIsNotNull();
            var rabbitIndex = (int) (Math.random() * rabbits.size());
            var rabbitPos = rabbits.get(rabbitIndex).getPosition();

            final double SPREAD = 20.0;

            var r = new Random();
            var pos = new Position();
            pos.setX(rabbitPos.getX() + (int) (r.nextGaussian() * SPREAD));
            pos.setY(rabbitPos.getY() + (int) (r.nextGaussian() * SPREAD));
            positionRepo.save(pos);

            var carrot = new Carrot();
            carrot.setPosition(pos);
            carrotRepo.save(carrot);
        }

        // spawn a fox
        if (Math.random() < foxSpawnProb(rabbitCount, foxCount)) {
            if (rabbits == null) {
                rabbits = rabbitRepo.findByPositionIsNotNull();
            }
            var rabbitIndex = (int) (Math.random() * rabbits.size());
            var rabbitPos = rabbits.get(rabbitIndex).getPosition();

            final double SPREAD = 14.0;

            var r = new Random();
            var pos = new Position();
            pos.setX(rabbitPos.getX() + (int) (r.nextGaussian() * SPREAD));
            pos.setY(rabbitPos.getY() + (int) (r.nextGaussian() * SPREAD));
            positionRepo.save(pos);

            var fox = new Fox();
            fox.setName(this.randomName(FOX_NAMES));
            fox.setPosition(pos);
            foxRepo.save(fox);
        }
    }

    private double carrotSpawnProb(int rabbitCount, int carrotCount) {
        var carrotDeficiency = 1.0 / (carrotCount + 1);
        var rabbitDeficiency = 1.0 / (rabbitCount + 1);
        var spawnProb = (carrotDeficiency - rabbitDeficiency + 1.0) / 2;
        return spawnProb;
    }

    public double foxSpawnProb(int rabbitCount, int foxCount) {
        var spawnProb = Math.pow(Math.E, -(1.0 + foxCount) / (1.0 + rabbitCount));
        return spawnProb;
    }

    public String randomName(String[] candidates) {
        var r = new Random();
        return candidates[r.nextInt(candidates.length)];
    }

    static final String[] RABBIT_NAMES = new String[] {"Ryan", "Roman", "Robert", "Rowan", "River",
            "Ryder", "Rhett", "Ryker", "Ross", "Richard", "Remington", "Rafael", "Rafferty",
            "Rupert", "Ralph", "Rufus", "Riley", "Ronan", "Rory", "Reid", "Raymond", "Rylan",
            "Roscoe", "Ricardo", "Romeo", "Russell", "Remy", "Royal", "Remus", "Raoul", "Rune",
            "Royce", "Ravi", "Rian", "Rami", "Rui", "Reginald", "Ren", "Romeo", "Rhodes", "Raiden",
            "Royce", "Rhys", "Reed", "Ronin", "Ruben", "Rain", "Rocco", "Roberto", "Raphael",
            "Rowen", "Roy", "Ridge", "Ronald", "Ryland", "Rodrigo", "Reece", "Reign", "Raul",
            "Rayan", "Romulus", "Ragnar", "Rio", "Roger", "Roland", "Rex", "Rayden", "Reese",
            "Remi", "Rey", "Ricky", "Rome", "Rohan", "Ramsey", "Rudy", "Rafi", "Rasmus", "Rollo",
            "Roderick", "Ridge", "Roan", "Rainier", "Riven", "Ray", "Rocky", "Reuben", "Robin",
            "Randy", "Rodney", "Redmond", "Rudolph", "Rishi", "Ramses", "Rogan", "Ranger",
            "Rockwell", "Reggie", "Rene", "Rye", "Ronnie", "Ryland", "Riley", "Ruby", "Rylee",
            "Raelynn", "Rose", "Remi", "Reagan", "Reese", "Roxanne", "Ria", "River", "Radha",
            "Ryleigh", "Rosalie", "Rachel", "Rowan", "Raegan", "Rebecca", "Remington", "Reign",
            "Rosemary", "Raelyn", "Rita", "Royalty", "Rio", "Reverie", "Rory", "Raven", "Renee",
            "Regina", "Rosie", "Raya", "Runa", "Rapunzel", "Rosamund", "Romilly", "Ryan", "Renata",
            "Ruth", "Remy", "Reyna", "Reina", "Renley", "Romina", "Roise", "Rumaysa", "Rhea",
            "Rosalia", "Rin", "Rosa", "Romina", "Rayna", "Roselyn", "Rina", "Rivka", "Rafaela",
            "Renesmee", "Rosemarie", "Roisin", "Ramona", "Rayne", "Rosalee", "Rhya", "Rena",
            "Rebekah", "Rosalyn", "Royal", "Ryann", "Romy", "Raina", "Raquel", "Rahma", "Rihanna",
            "Robin", "Rylan", "Roberta", "Ryder", "Rosalind", "Rue", "Rhiannon", "Rain", "Rosaline",
            "Rika", "Reshma", "Rhonda", "Ripley", "Ray", "Rowena", "Rory", "Rumi", "Ren", "Roxie",
            "Rowe", "Rhys", "Rochelle", "Riella", "Riona", "Rainbow",};

    static final String[] FOX_NAMES = new String[] {"Faith", "Freya", "Florence", "Flora", "Faye",
            "Frances", "Fayola", "Felicity", "Femi", "Fiona", "Francesca", "Fiadh", "Fahima",
            "Farah", "Fidda", "Finola", "Finley", "Fleur", "Fia", "Fern", "Frankie", "Francisca",
            "Fallon", "Fay", "Frida", "Farrah", "Fatima", "Fawn", "Fernanda", "Fannie", "Filomena",
            "Freda", "Fen", "Fabiana", "Fumie", "Fuyuko", "Felicia", "Fiammetta", "Francene",
            "Frederica", "Fae", "Fujie", "Fuyumi", "Flory", "Flossie", "Feba", "Fantasia", "Fari",
            "Faika", "Flavia", "Felise", "Flo", "Fermina", "Fran", "Farran", "Fiorenza", "Faustina",
            "Florencia", "Felicidad", "Füsun", "Felipa", "Fidela", "Fina", "Fieke", "Fritzi",
            "Finlay", "Florry", "Fang", "Ferne", "Farzana", "Fauna", "Foster", "Fatimah",
            "Fiorella", "Fallyn", "Floella", "Fortuna", "Ferna", "Faiza", "Fritzie", "Farren",
            "Floretta", "Fiori", "Florita", "Fancie", "Felysse", "Fala", "Fenella", "Fernly",
            "Flanna", "Fila", "Filipina", "Fillys", "Finette", "Fernleigh", "Floribeth", "Fantine",
            "Francziska", "Filippine", "Feleicia", "Finn", "Ford", "Finnegan", "Frank", "Freddie",
            "Fredric", "Franklin", "Fletcher", "Fred", "Fitz", "Fenton", "Felix", "Fernão",
            "Fabian", "Faron", "Fenix", "Fedor", "Fishel", "Fulton", "Farrin", "Felipe", "Fischer",
            "Franco", "Fidel", "Florencio", "Flavio", "Filomeno", "Fraser", "Fox", "Fortune",
            "Fayiz", "Fuller", "Field", "Free", "Fachtna", "Fergal", "Floki", "Fukashi", "Fumihiro",
            "Fabio", "Fabrizio", "Faustino", "Fujio", "Fumio", "Fumito", "Fuyu", "Fen", "Farrell",
            "Fitzroy", "Faruq", "Felice", "Finneas", "Forrest", "Fayaz", "Farley", "Fenn", "Falken",
            "Fallow", "Falkner", "Farnell", "Flanagan", "Finley", "Fernando", "Farid", "Fuad",
            "Francisco", "Facundo", "Frances", "Fikri", "Fawzi", "Francis", "Finnian", "Festus",
            "Frankie", "Flynn", "Frederick", "Fingal", "Fallon", "Fitzgerald", "Floyd", "Fryer",
            "Foster", "Fiore", "Fosco", "Fonz", "Ferdinand", "Figaro", "Falk", "Franzel", "Fadi",
            "Fergus", "Floro", "Freyr", "Ferdie", "Foxen", "Franche", "Fran", "Frido", "Frye",
            "Frewin",};
}
