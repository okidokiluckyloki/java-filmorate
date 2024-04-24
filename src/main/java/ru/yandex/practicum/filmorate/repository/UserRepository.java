package ru.yandex.practicum.filmorate.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class UserRepository {

    private JdbcTemplate template;

    public User create(User user) {
        template.update(
                "insert into users (name, login, email, birthday) values(?, ?, ?, ?)",
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                Date.from(user.getBirthday().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        Integer userId = template.queryForObject(
                "select max(id) as max from users", (rs, rowNum) -> rs.getInt("max"));
        user.setId(userId);
        log.info("save user '{}' to table 'users'", userId);
        return user;
    }

    public User update(User user) {
        throwNotFoundExceptionForNonExistentUserId(user.getId());
        template.update(
                "update users set name = ?, login = ?, email = ?, birthday = ? where id = ?",
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                Date.from(user.getBirthday().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                user.getId());
        List<Integer> followersList = template.query(
                "select following_id from follows where followed_id = ?",
                ((rs, rowNum) -> rs.getInt("following_id")), user.getId());
        user.getFriends().addAll(followersList);
        log.info("update user '{}' in table 'users'", user.getId());
        return user;
    }


    public User getById(Integer id) {
        throwNotFoundExceptionForNonExistentUserId(id);
        User user = template.queryForObject(
                "select * from users where id = ?",
                userWithoutFollowersRowMapper(), id);
        setFollowersIdsFromDateBase(user);
        log.info("show user '{}'", id);
        return user;
    }

    public List<User> findAll() {
        List<User> users = template.query(
                "select * from users order by id asc",
                userWithoutFollowersRowMapper());
        users.forEach(this::setFollowersIdsFromDateBase);
        return users;
    }

    public User addFollow(Integer userId, Integer friendId) {
        throwNotFoundExceptionForNonExistentUserId(userId);
        throwNotFoundExceptionForNonExistentUserId(friendId);
        template.update(
                "insert into follows (following_id, followed_id) values(?, ?)",
                friendId, userId);
        log.info("subscribe user '{}' to user '{}'", userId, friendId);
        return getById(userId);
    }

    public User removeFollowing(Integer userId, Integer friendId) {
        throwNotFoundExceptionForNonExistentUserId(userId);
        throwNotFoundExceptionForNonExistentUserId(friendId);
        template.update(
                "delete from follows where following_id = ? and followed_id = ?",
                friendId, userId);
        log.info("unsubscribe user '{}' from user '{}'", userId, friendId);
        return getById(userId);
    }

    public List<User> getSameFollowers(Integer userId, Integer friendId) {
        throwNotFoundExceptionForNonExistentUserId(userId);
        throwNotFoundExceptionForNonExistentUserId(friendId);
        List<User> sameFollowers = template.query(
                "select * from users where id in " +
                        "(select following_id from follows where followed_id = ?) " +
                        "and id in " +
                        "(select following_id from follows where followed_id = ?)",
                userWithoutFollowersRowMapper(), userId, friendId);
        sameFollowers.forEach(this::setFollowersIdsFromDateBase);
        log.info("show same followers of user '{}' and user '{}'", userId, friendId);
        return sameFollowers;
    }

    public List<User> getFollowers(Integer userId) {
        throwNotFoundExceptionForNonExistentUserId(userId);
        List<User> followers = template.query(
                "select * from users where id in " +
                        "(select following_id from follows where followed_id = ?)",
                userWithoutFollowersRowMapper(), userId);
        followers.forEach(this::setFollowersIdsFromDateBase);
        log.info("show followers of user '{}'", userId);
        return followers;
    }

    private RowMapper<User> userWithoutFollowersRowMapper() {
        return (rs, rowNum) -> new User()
                .setId(rs.getInt("id"))
                .setName(rs.getString("name"))
                .setEmail(rs.getString("email"))
                .setLogin(rs.getString("login"))
                .setBirthday(rs.getDate("birthday").toLocalDate());
    }

    private void setFollowersIdsFromDateBase(User user) {
        user.getFriends().addAll(template.query(
                "select following_id from follows where followed_id = ?",
                (rs, rowNum) -> rs.getInt("following_id"), user.getId()));
    }

    private void throwNotFoundExceptionForNonExistentUserId(int id) {
        if (Boolean.FALSE.equals(template.queryForObject(
                "select exists (select id from users where id = ?) as match",
                (rs, rowNum) -> rs.getBoolean("match"), id))) {
            throw new NotFoundException("No users with such ID: " + id);
        }
    }
}