package edu.gatech.seclass.tourneymanager.mode;


import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.gatech.seclass.tourneymanager.db.DatabaseHelper;
import edu.gatech.seclass.tourneymanager.model.Deck;
import edu.gatech.seclass.tourneymanager.model.Player;
import edu.gatech.seclass.tourneymanager.model.Tournament;
import edu.gatech.seclass.tourneymanager.utils.TourneyCalcAlgorithm;

public class ManagerMode {
    Context context;
    Tournament ongoingTournament;

    public ManagerMode(Context context) {
       this.context = context;
    }

    public boolean thereIsOngoingTournament() throws SQLException {
        DatabaseHelper dbHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        RuntimeExceptionDao<Tournament, Integer> tournamentDao= dbHelper.getTournamentRuntimeExceptionDao();

        List<Tournament> tournamentList = tournamentDao.queryBuilder()
                .where().eq("status", Tournament.TournamentStatus.ONGOING).query();

        OpenHelperManager.releaseHelper();

        if (tournamentList.size() == 0) {
            return false;
        }
        else {
            // Save for other use in the class
            ongoingTournament = tournamentList.get(0);

            return true;
        }
    }

    public void addPlayerToSystem(String username, String name, String phoneNumber, Deck deck) {
        DatabaseHelper dbHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        RuntimeExceptionDao<Player, String> playerDao= dbHelper.getPlayerRuntimeExceptionDao();

        playerDao.create(new Player(username, name, phoneNumber, deck));

        OpenHelperManager.releaseHelper();
    }

    public void removePlayerFromSystem(String username) throws SQLException {
        DatabaseHelper dbHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        RuntimeExceptionDao<Player, String> playerDao= dbHelper.getPlayerRuntimeExceptionDao();

        DeleteBuilder db = playerDao.deleteBuilder();
        db.where().eq("username", username);
        playerDao.delete(db.prepare());

        OpenHelperManager.releaseHelper();
    }

    public Map<String, Integer> showTournamentInfo(int entranceFee, int numEntrants, int housePercentage) {
        // Use hashmap instead of array for easier reference
        Map<String, Integer> tournamentInfo = new HashMap<String, Integer>();

        int totalAmount = TourneyCalcAlgorithm.calcTotalAmount(entranceFee, numEntrants);
        int houseCut = TourneyCalcAlgorithm.calcHouseCut(totalAmount, housePercentage);
        int[] prizes = TourneyCalcAlgorithm.calcPrizes(totalAmount, houseCut);

        tournamentInfo.put("houseCut", houseCut);
        tournamentInfo.put("firstPrize", prizes[0]);
        tournamentInfo.put("secondPrize", prizes[1]);
        tournamentInfo.put("thirdPrize", prizes[2]);

        return tournamentInfo;
    }

    public void createTournament(int houseCut, int entryPrice, String username) {
        DatabaseHelper dbHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        RuntimeExceptionDao<Tournament, Integer> tournamentDao= dbHelper.getTournamentRuntimeExceptionDao();

        tournamentDao.create(new Tournament(houseCut, entryPrice, username));

        OpenHelperManager.releaseHelper();
    }

    public void getOngoingTournament() {

    }

    public void endOngoingTournament() {
        if (this.ongoingTournament == null) {
            throw new IllegalStateException("There is no ongoing tournament.");
        }

        DatabaseHelper dbHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        RuntimeExceptionDao<Tournament, Integer> tournamentDao= dbHelper.getTournamentRuntimeExceptionDao();

        //TODO: check if tournament has ended prematurely and money needs to be refunded

        this.ongoingTournament.endTournament();
        tournamentDao.update(this.ongoingTournament);
    }
}