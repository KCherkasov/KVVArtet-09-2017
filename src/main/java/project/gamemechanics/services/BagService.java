package project.gamemechanics.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.postgresql.util.PGobject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import project.gamemechanics.globals.Constants;
import project.gamemechanics.services.dbmodels.BagDatabaseModel;
import project.gamemechanics.services.interfaces.BagDAO;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

@Service
public class BagService implements BagDAO {

    private final Logger logger = Logger.getLogger(BagService.class);
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public BagService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BagDatabaseModel getBagById(@NotNull Integer id) {
        final String sql = "SELECT * FROM public.bag WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (ResultSet rs, int rwNumber) -> {
            BagDatabaseModel bagDatabaseModel = null;
            try {
                bagDatabaseModel = mapper.readValue(rs.getString("bag_json_model"), BagDatabaseModel.class);
            } catch (IOException e) {
                logger.trace(e);
            }
            return bagDatabaseModel;
        });
    }

    @Override
    public Integer setBag(@NotNull BagDatabaseModel newBag) {
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final String sql = "INSERT into public.bag(bag_json_model)" + "values(?) RETURNING id";
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                try {
                    PGobject jsonObject = new PGobject();
                    jsonObject.setType("json");
                    jsonObject.setValue(mapper.writeValueAsString(newBag));
                    preparedStatement.setObject(1, jsonObject);
                } catch (JsonProcessingException e) {
                    logger.trace(e);
                }
                return preparedStatement;
            }, keyHolder);
        } catch (DataAccessException e) {
            logger.trace(e);
            return Constants.UNDEFINED_ID;
        }
        return keyHolder.getKey().intValue();
    }

    @Override
    @SuppressWarnings("Duplicates")
    public void updateSlotsInBag(@NotNull Integer bagId, @NotNull List<Pair<Integer, Integer>> indexToIdItemsList) {
        final String sql = "UPDATE public.bag SET bag_json_model = ? WHERE id = ?";
        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                BagDatabaseModel bagDatabaseModel = getBagById(bagId);
                bagDatabaseModel.updateSlots(indexToIdItemsList);
                PGobject jsonObject = new PGobject();
                jsonObject.setType("json");
                jsonObject.setValue(mapper.writeValueAsString(bagDatabaseModel));
                preparedStatement.setObject(1, jsonObject);
                preparedStatement.setObject(2, bagId);
            } catch (JsonProcessingException e) {
                logger.trace(e);
            }
            return preparedStatement;
        });
    }

    public void deleteBag(Integer id) {
        final String sql = "DELETE FROM public.bag WHERE id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, id);
            return preparedStatement;
        });
    }
}
